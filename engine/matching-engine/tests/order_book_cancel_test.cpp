#include "marketforge/order_book.hpp"

#include <cassert>

int main() {
    using namespace marketforge;

    OrderBook book;

    book.add({
        1, "BUY-1", Side::BUY, OrderType::LIMIT,
        TimeInForce::DAY, 10000, 10
    });

    book.add({
        2, "BUY-2", Side::BUY, OrderType::LIMIT,
        TimeInForce::DAY, 10100, 10
    });

    assert(book.size() == 2);
    assert(book.best_bid()->id == 2);

    assert(book.cancel(2));
    assert(book.size() == 1);
    assert(book.best_bid()->id == 1);

    assert(!book.cancel(999));
    assert(book.size() == 1);

    return 0;
}
