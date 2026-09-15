#include "marketforge/order_book.hpp"

#include <cassert>

int main() {
    using namespace marketforge;

    OrderBook book;

    assert(book.add({
        1, "BUY-1", Side::BUY, OrderType::LIMIT,
        TimeInForce::DAY, 10000, 10
    }));

    assert(book.add({
        2, "BUY-2", Side::BUY, OrderType::LIMIT,
        TimeInForce::DAY, 10100, 10
    }));

    assert(book.add({
        3, "SELL-1", Side::SELL, OrderType::LIMIT,
        TimeInForce::DAY, 10200, 10
    }));

    assert(book.add({
        4, "SELL-2", Side::SELL, OrderType::LIMIT,
        TimeInForce::DAY, 10300, 10
    }));

    assert(book.size() == 4);
    assert(book.best_bid()->id == 2);
    assert(book.best_ask()->id == 3);

    return 0;
}
