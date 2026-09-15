#pragma once

#include <atomic>
#include <cstdint>
#include <string>

namespace marketforge {

class TradeIdGenerator {
public:
    TradeIdGenerator();

    std::string next();

private:
    std::string instance_id_;
    std::atomic<std::uint64_t> next_id_{1};
};

} // namespace marketforge
