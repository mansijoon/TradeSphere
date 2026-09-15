#pragma once

#include <cstdint>
#include <string>

namespace marketforge {

enum class Side {
    BUY,
    SELL
};

enum class OrderType {
    MARKET,
    LIMIT
};

enum class TimeInForce {
    DAY,
    GTC,
    IOC,
    FOK
};

enum class OrderStatus {
    NEW,
    ACCEPTED,
    PARTIALLY_FILLED,
    FILLED,
    CANCELLED,
    REJECTED
};

struct Order {
    std::uint64_t id;
    std::string client_order_id;
    Side side;
    OrderType type;
    TimeInForce time_in_force;
    std::int64_t price_ticks;
    std::int64_t quantity;
    std::int64_t filled_quantity{0};

    [[nodiscard]]
    std::int64_t remaining_quantity() const {
        return quantity - filled_quantity;
    }
};

} // namespace marketforge
