#pragma once

#include "marketforge/trade.hpp"

#include <string>

namespace marketforge {

class KafkaProducer {
public:
    KafkaProducer(std::string brokers, std::string topic);

    ~KafkaProducer();

    void publish(const Trade& trade, const std::string& instrument_id);

private:
    std::string brokers_;
    std::string topic_;

    struct Impl;
    Impl* impl_;
};

} // namespace marketforge
