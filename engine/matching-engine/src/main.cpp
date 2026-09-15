#include "marketforge/kafka_consumer.hpp"

#include <cstdlib>
#include <iostream>
#include <string>

int main() {
    const char* brokers_env = std::getenv("KAFKA_BROKERS");
    const char* topic_env = std::getenv("KAFKA_TOPIC");

    const std::string brokers =
        brokers_env ? brokers_env : "localhost:31092";

    const std::string topic =
        topic_env ? topic_env : "order.commands";

    marketforge::KafkaConsumer consumer(
        brokers,
        topic,
        "marketforge-matching-engine"
    );

    consumer.run();

    return 0;
}
