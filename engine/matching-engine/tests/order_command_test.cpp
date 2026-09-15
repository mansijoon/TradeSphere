#include "marketforge/order_command.hpp"

#include <cassert>

int main() {
    using namespace marketforge;

    OrderCommand command{
        1001,
        2001,
        3001,
        Side::BUY,
        OrderType::LIMIT,
        TimeInForce::DAY,
        185000,
        10
    };

    assert(command.order_id == 1001);
    assert(command.account_id == 2001);
    assert(command.instrument_id == 3001);
    assert(command.side == Side::BUY);
    assert(command.type == OrderType::LIMIT);
    assert(command.price_ticks == 185000);
    assert(command.quantity == 10);

    return 0;
}
