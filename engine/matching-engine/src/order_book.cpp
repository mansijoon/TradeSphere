#include "marketforge/order_book.hpp"

namespace marketforge {

bool OrderBook::add(Order order) {
    if (order.quantity <= 0 || order.remaining_quantity() <= 0) {
        return false;
    }

    auto stored = std::make_shared<Order>(std::move(order));
    const auto id = stored->id;
    const auto side = stored->side;
    const auto price_ticks = stored->price_ticks;

    if (side == Side::BUY) {
        auto& level = bids_[price_ticks];
        const auto index = level.orders.size();
        level.orders.push_back(std::move(stored));
        orders_by_id_[id] = {Side::BUY, price_ticks, index};
    } else {
        auto& level = asks_[price_ticks];
        const auto index = level.orders.size();
        level.orders.push_back(std::move(stored));
        orders_by_id_[id] = {Side::SELL, price_ticks, index};
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

    auto& level = bids_.begin()->second;

    while (level.head < level.orders.size() &&
           level.orders[level.head] == nullptr) {
        ++level.head;
    }

    if (level.head == level.orders.size()) {
        return nullptr;
    }

    return level.orders[level.head].get();
}

const Order* OrderBook::best_bid() const {
    if (bids_.empty()) {
        return nullptr;
    }

    const auto& level = bids_.begin()->second;
    auto head = level.head;

    while (head < level.orders.size() &&
           level.orders[head] == nullptr) {
        ++head;
    }

    if (head == level.orders.size()) {
        return nullptr;
    }

    return level.orders[head].get();
}

Order* OrderBook::best_ask() {
    if (asks_.empty()) {
        return nullptr;
    }

    auto& level = asks_.begin()->second;

    while (level.head < level.orders.size() &&
           level.orders[level.head] == nullptr) {
        ++level.head;
    }

    if (level.head == level.orders.size()) {
        return nullptr;
    }

    return level.orders[level.head].get();
}

const Order* OrderBook::best_ask() const {
    if (asks_.empty()) {
        return nullptr;
    }

    const auto& level = asks_.begin()->second;
    auto head = level.head;

    while (head < level.orders.size() &&
           level.orders[head] == nullptr) {
        ++head;
    }

    if (head == level.orders.size()) {
        return nullptr;
    }

    return level.orders[head].get();
}

void OrderBook::remove_best_bid() {
    if (bids_.empty()) {
        return;
    }

    auto it = bids_.begin();
    auto& level = it->second;

    while (level.head < level.orders.size() &&
           level.orders[level.head] == nullptr) {
        ++level.head;
    }

    if (level.head == level.orders.size()) {
        bids_.erase(it);
        return;
    }

    auto& order = level.orders[level.head];

    orders_by_id_.erase(order->id);
    order.reset();
    ++level.head;

    if (level.head == level.orders.size()) {
        bids_.erase(it);
    }

    --order_count_;
}

void OrderBook::remove_best_ask() {
    if (asks_.empty()) {
        return;
    }

    auto it = asks_.begin();
    auto& level = it->second;

    while (level.head < level.orders.size() &&
           level.orders[level.head] == nullptr) {
        ++level.head;
    }

    if (level.head == level.orders.size()) {
        asks_.erase(it);
        return;
    }

    auto& order = level.orders[level.head];

    orders_by_id_.erase(order->id);
    order.reset();
    ++level.head;

    if (level.head == level.orders.size()) {
        asks_.erase(it);
    }

    --order_count_;
}

bool OrderBook::cancel(std::uint64_t order_id) {
    const auto location_it = orders_by_id_.find(order_id);

    if (location_it == orders_by_id_.end()) {
        return false;
    }

    const auto location = location_it->second;

    if (location.side == Side::BUY) {
        auto level_it = bids_.find(location.price_ticks);

        if (level_it == bids_.end()) {
            return false;
        }

        auto& level = level_it->second;

        if (location.index >= level.orders.size() ||
            level.orders[location.index] == nullptr ||
            level.orders[location.index]->id != order_id) {
            return false;
        }

        level.orders[location.index].reset();
        orders_by_id_.erase(location_it);

        if (location.index == level.head) {
            while (level.head < level.orders.size() &&
                   level.orders[level.head] == nullptr) {
                ++level.head;
            }
        }

        if (level.head == level.orders.size()) {
            bids_.erase(level_it);
        }

        --order_count_;
        return true;
    }

    auto level_it = asks_.find(location.price_ticks);

    if (level_it == asks_.end()) {
        return false;
    }

    auto& level = level_it->second;

    if (location.index >= level.orders.size() ||
        level.orders[location.index] == nullptr ||
        level.orders[location.index]->id != order_id) {
        return false;
    }

    level.orders[location.index].reset();
    orders_by_id_.erase(location_it);

    if (location.index == level.head) {
        while (level.head < level.orders.size() &&
               level.orders[level.head] == nullptr) {
            ++level.head;
        }
    }

    if (level.head == level.orders.size()) {
        asks_.erase(level_it);
    }

    --order_count_;
    return true;
}

} // namespace marketforge
