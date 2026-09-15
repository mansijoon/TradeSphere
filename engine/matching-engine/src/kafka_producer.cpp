#include "marketforge/kafka_producer.hpp"

#include <chrono>
#include <ctime>
#include <iomanip>
#include <iostream>
#include <sstream>
#include <stdexcept>
#include <utility>

#include <librdkafka/rdkafka.h>
#include <nlohmann/json.hpp>

namespace marketforge {

struct KafkaProducer::Impl {
    rd_kafka_t* producer{nullptr};

    ~Impl() {
        if (producer) {
            rd_kafka_flush(producer, 5000);
            rd_kafka_destroy(producer);
        }
    }
};

KafkaProducer::KafkaProducer(
    std::string brokers,
    std::string topic
)
    : brokers_(std::move(brokers)),
      topic_(std::move(topic)),
      impl_(new Impl()) {

    rd_kafka_conf_t* conf = rd_kafka_conf_new();
    char errstr[512];

    auto set_config = [&](const char* key, const char* value) {
        if (rd_kafka_conf_set(
                conf,
                key,
                value,
                errstr,
                sizeof(errstr)) != RD_KAFKA_CONF_OK) {
            rd_kafka_conf_destroy(conf);
            throw std::runtime_error(
                std::string("Kafka configuration error: ") + errstr);
        }
    };

    set_config("bootstrap.servers", brokers_.c_str());
    set_config("security.protocol", "ssl");
    set_config("ssl.ca.location", "/tmp/redpanda-ca.crt");
    set_config("ssl.endpoint.identification.algorithm", "none");

    impl_->producer = rd_kafka_new(
        RD_KAFKA_PRODUCER,
        conf,
        errstr,
        sizeof(errstr));

    if (!impl_->producer) {
        throw std::runtime_error(
            std::string("Failed to create Kafka producer: ") + errstr);
    }
}

KafkaProducer::~KafkaProducer() {
    delete impl_;
}

void KafkaProducer::publish(const Trade& trade, const std::string& instrument_id) {
    const auto now = std::chrono::system_clock::now();
    const auto time_t_now = std::chrono::system_clock::to_time_t(now);

    std::tm utc_time{};
    gmtime_r(&time_t_now, &utc_time);

    std::ostringstream timestamp;
    timestamp << std::put_time(&utc_time, "%Y-%m-%dT%H:%M:%SZ");

    nlohmann::json event{
        {"eventType", "TradeExecuted"},
        {"eventVersion", 1},
        {"tradeId", trade.trade_id},
        {"takerOrderId", trade.taker_order_uuid},
        {"makerOrderId", trade.maker_order_uuid},
        {"instrumentId", instrument_id},
        {"price", static_cast<double>(trade.price_ticks) / 10000.0},
        {"quantity", static_cast<double>(trade.quantity) / 10000.0},
        {"timestamp", timestamp.str()}
    };

    const std::string payload = event.dump();
    const std::string key = trade.trade_id;

    const rd_kafka_resp_err_t error = rd_kafka_producev(
        impl_->producer,
        RD_KAFKA_V_TOPIC(topic_.c_str()),
        RD_KAFKA_V_MSGFLAGS(RD_KAFKA_MSG_F_COPY),
        RD_KAFKA_V_KEY(key.data(), key.size()),
        RD_KAFKA_V_VALUE(const_cast<char*>(payload.data()), payload.size()),
        RD_KAFKA_V_END);

    if (error != RD_KAFKA_RESP_ERR_NO_ERROR) {
        throw std::runtime_error(
            std::string("Failed to publish trade: ") +
            rd_kafka_err2str(error));
    }

    rd_kafka_poll(impl_->producer, 0);

    std::cout << "Published TradeExecuted"
              << " tradeId=" << trade.trade_id
              << " quantity=" << trade.quantity
              << '\n';
}

} // namespace marketforge
