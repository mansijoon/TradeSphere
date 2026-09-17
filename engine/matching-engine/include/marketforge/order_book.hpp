#pragma once

#include "marketforge/order.hpp"

#include <cstdint>
#include <map>
#include <memory>
#include <unordered_map>
#include <vector>

namespace marketforge {

class OrderBook {
public:
    bool add(Order order);

    bool cancel(std::uint64_t order_id);

    [[nodiscard]]
    std::size_t size() const;

    [[nodiscard]]
    Order* best_bid();

    [[nodiscard]]
    const Order* best_bid() const;

    void remove_best_bid();

    [[nodiscard]]
    Order* best_ask();

    [[nodiscard]]
    const Order* best_ask() const;

    void remove_best_ask();

private:
    using OrderPtr = std::shared_ptr<Order>;

    struct PriceLevel {
        std::vector<OrderPtr> orders;
        std::size_t head{0};
    };

    struct BuyCompare {
        bool operator()(std::int64_t lhs, std::int64_t rhs) const {
            return lhs > rhs;
        }
    };

    struct OrderLocation {
        Side side;
        std::int64_t price_ticks;
        std::size_t index;
    };

    std::map<std::int64_t, PriceLevel, BuyCompare> bids_;
    std::map<std::int64_t, PriceLevel> asks_;
    std::unordered_map<std::uint64_t, OrderLocation> orders_by_id_;
    std::size_t order_count_{0};
};

} // namespace marketforge
