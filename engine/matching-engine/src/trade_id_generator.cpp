#include "marketforge/trade_id_generator.hpp"

#include <array>
#include <random>

namespace marketforge {

namespace {

std::string generate_instance_id() {
    std::random_device rd;
    std::array<std::uint32_t, 4> values{
        rd(), rd(), rd(), rd()
    };

    char buffer[37];
    std::snprintf(
        buffer,
        sizeof(buffer),
        "%08x-%04x-%04x-%04x-%08x%04x",
        values[0],
        static_cast<unsigned>(values[1] >> 16),
        static_cast<unsigned>(values[1] & 0xffff),
        static_cast<unsigned>(values[2] & 0xffff),
        values[3],
        static_cast<unsigned>(values[2] >> 16)
    );

    return buffer;
}

} // namespace

TradeIdGenerator::TradeIdGenerator()
    : instance_id_(generate_instance_id()) {}

std::string TradeIdGenerator::next() {
    return instance_id_ + "-" + std::to_string(
        next_id_.fetch_add(1, std::memory_order_relaxed)
    );
}

} // namespace marketforge
