#include "marketforge/order.hpp"

#include <cassert>

int main() {
    using namespace marketforge;

    Order order{
        1,
        "TEST-001",
        Side::BUY,
        OrderType::LIMIT,
        TimeInForce::DAY,
        10000,
        50
    };

    assert(order.quantity == 50);
    assert(order.filled_quantity == 0);
    assert(order.remaining_quantity() == 50);

    order.filled_quantity = 20;

    assert(order.remaining_quantity() == 30);

    return 0;
}
