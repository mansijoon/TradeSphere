#pragma once

#include "marketforge/order.hpp"

#include <cstdint>
#include <map>
#include <memory>
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

    struct BuyCompare {
        bool operator()(std::int64_t lhs, std::int64_t rhs) const {
            return lhs > rhs;
        }
    };

    std::map<std::int64_t, std::vector<OrderPtr>, BuyCompare> bids_;
    std::map<std::int64_t, std::vector<OrderPtr>> asks_;
    std::size_t order_count_{0};
};

} // namespace marketforge
