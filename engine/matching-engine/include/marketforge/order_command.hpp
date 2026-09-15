#pragma once

#include "marketforge/order.hpp"

#include <cstdint>
#include <string>

namespace marketforge {

struct OrderCommand {
    std::uint64_t order_id;
    std::uint64_t account_id;
    std::uint64_t instrument_id;
    Side side;
    OrderType type;
    TimeInForce time_in_force;
    std::int64_t price_ticks;
    std::int64_t quantity;
};

} // namespace marketforge
