#include "marketforge/order_book.hpp"

namespace marketforge {

bool OrderBook::add(Order order) {
    if (order.quantity <= 0 || order.remaining_quantity() <= 0) {
        return false;
    }

    auto stored = std::make_shared<Order>(std::move(order));

    if (stored->side == Side::BUY) {
        bids_[stored->price_ticks].push_back(std::move(stored));
    } else {
        asks_[stored->price_ticks].push_back(std::move(stored));
    }

    ++order_count_;
    return true;
}

std::size_t OrderBook::size() const {
    return order_count_;
}

Order* OrderBook::best_bid() {
    if (bids_.empty()) {
        return nullptr;
    }

    return bids_.begin()->second.front().get();
}

const Order* OrderBook::best_bid() const {
    if (bids_.empty()) {
        return nullptr;
    }

    return bids_.begin()->second.front().get();
}

Order* OrderBook::best_ask() {
    if (asks_.empty()) {
        return nullptr;
    }

    return asks_.begin()->second.front().get();
}

const Order* OrderBook::best_ask() const {
    if (asks_.empty()) {
        return nullptr;
    }

    return asks_.begin()->second.front().get();
}

} // namespace marketforge

void marketforge::OrderBook::remove_best_bid() {
    if (bids_.empty()) {
        return;
    }

    auto& orders = bids_.begin()->second;
    orders.erase(orders.begin());

    if (orders.empty()) {
        bids_.erase(bids_.begin());
    }

    --order_count_;
}

void marketforge::OrderBook::remove_best_ask() {
    if (asks_.empty()) {
        return;
    }

    auto& orders = asks_.begin()->second;
    orders.erase(orders.begin());

    if (orders.empty()) {
        asks_.erase(asks_.begin());
    }

    --order_count_;
}

bool marketforge::OrderBook::cancel(std::uint64_t order_id) {
    for (auto it = bids_.begin(); it != bids_.end(); ++it) {
        auto& orders = it->second;

        for (auto order = orders.begin(); order != orders.end(); ++order) {
            if ((*order)->id == order_id) {
                orders.erase(order);

                if (orders.empty()) {
                    bids_.erase(it);
                }

                --order_count_;
                return true;
            }
        }
    }

    for (auto it = asks_.begin(); it != asks_.end(); ++it) {
        auto& orders = it->second;

        for (auto order = orders.begin(); order != orders.end(); ++order) {
            if ((*order)->id == order_id) {
                orders.erase(order);

                if (orders.empty()) {
                    asks_.erase(it);
                }

                --order_count_;
                return true;
            }
        }
    }

    return false;
}
