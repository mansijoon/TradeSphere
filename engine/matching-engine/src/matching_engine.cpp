#include "marketforge/matching_engine.hpp"

#include <algorithm>

namespace marketforge {

std::vector<Trade> MatchingEngine::submit(Order order) {
    std::vector<Trade> fills;

    if (order.type != OrderType::LIMIT || order.quantity <= 0) {
        return fills;
    }

    if (order.side == Side::BUY) {
        while (order.remaining_quantity() > 0) {
            Order* maker = book_.best_ask();

            if (maker == nullptr || order.price_ticks < maker->price_ticks) {
                break;
            }

            const auto quantity = std::min(
                order.remaining_quantity(),
                maker->remaining_quantity()
            );

            fills.push_back({
                trade_ids_.next(),
                order.id,
                maker->id,
                order.client_order_id,
                maker->client_order_id,
                maker->price_ticks,
                quantity
            });

            maker->filled_quantity += quantity;
            order.filled_quantity += quantity;

            if (maker->remaining_quantity() == 0) {
                book_.remove_best_ask();
            }
        }
    } else {
        while (order.remaining_quantity() > 0) {
            Order* maker = book_.best_bid();

            if (maker == nullptr || order.price_ticks > maker->price_ticks) {
                break;
            }

            const auto quantity = std::min(
                order.remaining_quantity(),
                maker->remaining_quantity()
            );

            fills.push_back({
                trade_ids_.next(),
                order.id,
                maker->id,
                order.client_order_id,
                maker->client_order_id,
                maker->price_ticks,
                quantity
            });

            maker->filled_quantity += quantity;
            order.filled_quantity += quantity;

            if (maker->remaining_quantity() == 0) {
                book_.remove_best_bid();
            }
        }
    }

    if (order.remaining_quantity() > 0) {
        book_.add(std::move(order));
    }

    return fills;
}

const OrderBook& MatchingEngine::book() const {
    return book_;
}

} // namespace marketforge
