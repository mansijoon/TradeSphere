#pragma once

#include "marketforge/order_book.hpp"
#include "marketforge/trade.hpp"
#include "marketforge/trade_id_generator.hpp"

#include <vector>

namespace marketforge {

class MatchingEngine {
public:
    std::vector<Trade> submit(Order order);

    [[nodiscard]]
    const OrderBook& book() const;

private:
    OrderBook book_;
    TradeIdGenerator trade_ids_;
};

} // namespace marketforge
