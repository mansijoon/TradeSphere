#pragma once

#include <cstdint>
#include <string>

namespace marketforge {

struct Trade {
    std::string trade_id;
    std::uint64_t taker_order_id;
    std::uint64_t maker_order_id;
    std::string taker_order_uuid;
    std::string maker_order_uuid;
    std::int64_t price_ticks;
    std::int64_t quantity;
};

} // namespace marketforge
