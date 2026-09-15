#pragma once

#include <string>

namespace marketforge {

class KafkaConsumer {
public:
    KafkaConsumer(
        std::string brokers,
        std::string topic,
        std::string group_id
    );

    void run();

private:
    std::string brokers_;
    std::string topic_;
    std::string group_id_;
};

} // namespace marketforge
