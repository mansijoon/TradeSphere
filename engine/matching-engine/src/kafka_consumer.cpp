#include "marketforge/kafka_consumer.hpp"

#include <cstdint>
#include <functional>
#include <iostream>
#include <string>
#include <utility>

#include <librdkafka/rdkafka.h>
#include <nlohmann/json.hpp>

#include "marketforge/matching_engine.hpp"
#include "marketforge/order_command.hpp"
#include "marketforge/kafka_producer.hpp"

namespace marketforge {

KafkaConsumer::KafkaConsumer(
    std::string brokers,
    std::string topic,
    std::string group_id
)
    : brokers_(std::move(brokers)),
      topic_(std::move(topic)),
      group_id_(std::move(group_id)) {}

void KafkaConsumer::run() {
    std::cout << "Kafka consumer configured\n"
              << "brokers: " << brokers_ << '\n'
              << "topic: " << topic_ << '\n'
              << "group: " << group_id_ << '\n';

    rd_kafka_conf_t* conf = rd_kafka_conf_new();

    char errstr[512];

    if (rd_kafka_conf_set(
            conf,
            "bootstrap.servers",
            brokers_.c_str(),
            errstr,
            sizeof(errstr)) != RD_KAFKA_CONF_OK) {
        std::cerr << "Kafka configuration error: "
                  << errstr << '\n';
        rd_kafka_conf_destroy(conf);
        return;
    }

    if (rd_kafka_conf_set(
            conf,
            "security.protocol",
            "ssl",
            errstr,
            sizeof(errstr)) != RD_KAFKA_CONF_OK) {
        std::cerr << "Kafka security configuration error: "
                  << errstr << '\n';
        rd_kafka_conf_destroy(conf);
        return;
    }

    if (rd_kafka_conf_set(
            conf,
            "ssl.ca.location",
            "/tmp/redpanda-ca.crt",
            errstr,
            sizeof(errstr)) != RD_KAFKA_CONF_OK) {
        std::cerr << "Kafka CA configuration error: "
                  << errstr << '\n';
        rd_kafka_conf_destroy(conf);
        return;
    }

    if (rd_kafka_conf_set(
            conf,
            "ssl.endpoint.identification.algorithm",
            "none",
            errstr,
            sizeof(errstr)) != RD_KAFKA_CONF_OK) {
        std::cerr << "Kafka TLS hostname configuration error: "
                  << errstr << '\n';
        rd_kafka_conf_destroy(conf);
        return;
    }

    if (rd_kafka_conf_set(
            conf,
            "group.id",
            group_id_.c_str(),
            errstr,
            sizeof(errstr)) != RD_KAFKA_CONF_OK) {
        std::cerr << "Kafka group configuration error: "
                  << errstr << '\n';
        rd_kafka_conf_destroy(conf);
        return;
    }

    rd_kafka_t* consumer = rd_kafka_new(
        RD_KAFKA_CONSUMER,
        conf,
        errstr,
        sizeof(errstr));

    if (!consumer) {
        std::cerr << "Failed to create Kafka consumer: "
                  << errstr << '\n';
        return;
    }

    rd_kafka_topic_partition_list_t* topics =
        rd_kafka_topic_partition_list_new(1);

    rd_kafka_topic_partition_list_add(
        topics,
        topic_.c_str(),
        RD_KAFKA_PARTITION_UA);

    const rd_kafka_resp_err_t error =
        rd_kafka_subscribe(consumer, topics);

    rd_kafka_topic_partition_list_destroy(topics);

    if (error != RD_KAFKA_RESP_ERR_NO_ERROR) {
        std::cerr << "Failed to subscribe: "
                  << rd_kafka_err2str(error) << '\n';
        rd_kafka_destroy(consumer);
        return;
    }

    std::cout << "Subscribed successfully.\n";

    MatchingEngine engine;
    KafkaProducer producer(brokers_, "trade.executed");

    while (true) {
        rd_kafka_message_t* message =
            rd_kafka_consumer_poll(consumer, 1000);

        if (!message) {
            continue;
        }

        if (message->err) {
            std::cerr << "Kafka error: "
                      << rd_kafka_message_errstr(message)
                      << '\n';

            rd_kafka_message_destroy(message);
            continue;
        }

        try {
            const std::string payload(
                static_cast<const char*>(message->payload),
                message->len);

            const auto json = nlohmann::json::parse(payload);

            const std::string order_id =
                json.at("orderId").get<std::string>();

            const std::string account_id =
                json.at("accountId").get<std::string>();

            const std::string instrument_id =
                json.at("instrumentId").get<std::string>();

            const std::string side_text =
                json.at("side").get<std::string>();

            const std::string type_text =
                json.at("orderType").get<std::string>();

            const std::string tif_text =
                json.at("timeInForce").get<std::string>();

            const Side side =
                side_text == "BUY" ? Side::BUY : Side::SELL;

            const OrderType type =
                type_text == "LIMIT"
                    ? OrderType::LIMIT
                    : OrderType::MARKET;

            TimeInForce tif = TimeInForce::DAY;

            if (tif_text == "GTC") {
                tif = TimeInForce::GTC;
            } else if (tif_text == "IOC") {
                tif = TimeInForce::IOC;
            } else if (tif_text == "FOK") {
                tif = TimeInForce::FOK;
            }

            const double price =
                json.at("price").is_null()
                    ? 0.0
                    : json.at("price").get<double>();

            const double quantity =
                json.at("quantity").get<double>();

            const auto uuid_to_id =
                [](const std::string& value) -> std::uint64_t {
                    return static_cast<std::uint64_t>(
                        std::hash<std::string>{}(value));
                };

            OrderCommand command{
                uuid_to_id(order_id),
                uuid_to_id(account_id),
                uuid_to_id(instrument_id),
                side,
                type,
                tif,
                static_cast<std::int64_t>(price * 10000.0),
                static_cast<std::int64_t>(quantity * 10000.0)
            };

            std::cout << "OrderCommand created\n"
                      << "  orderId: " << command.order_id << '\n'
                      << "  accountId: " << command.account_id << '\n'
                      << "  instrumentId: " << command.instrument_id << '\n'
                      << "  priceTicks: " << command.price_ticks << '\n'
                      << "  quantity: " << command.quantity << '\n';

            Order order{
                command.order_id,
                order_id,
                command.side,
                command.type,
                command.time_in_force,
                command.price_ticks,
                command.quantity
            };

            const auto trades = engine.submit(order);

            std::cout << "Matching engine result: "
                      << trades.size() << " trade(s)\n";

            for (const auto& trade : trades) {
                std::cout << "  tradeId: " << trade.trade_id
                          << " priceTicks: " << trade.price_ticks
                          << " quantity: " << trade.quantity
                          << '\n';

                producer.publish(trade, instrument_id);
            }
        } catch (const nlohmann::json::exception& e) {
            std::cerr << "Invalid order command JSON: "
                      << e.what() << '\n';
        }

        rd_kafka_message_destroy(message);
    }
}

} // namespace marketforge
