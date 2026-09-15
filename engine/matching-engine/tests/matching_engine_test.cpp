#include "marketforge/matching_engine.hpp"

#include <cassert>

int main() {
    using namespace marketforge;

    MatchingEngine engine;

    engine.submit({
        1, "SELL-1", Side::SELL, OrderType::LIMIT,
        TimeInForce::DAY, 10000, 5
    });

    engine.submit({
        2, "SELL-2", Side::SELL, OrderType::LIMIT,
        TimeInForce::DAY, 10000, 5
    });

    auto trades = engine.submit({
        3, "BUY-1", Side::BUY, OrderType::LIMIT,
        TimeInForce::DAY, 10100, 8
    });

    assert(trades.size() == 2);

    assert(trades[0].trade_id.ends_with("-1"));
    assert(trades[0].taker_order_id == 3);
    assert(trades[0].maker_order_id == 1);
    assert(trades[0].price_ticks == 10000);
    assert(trades[0].quantity == 5);

    assert(trades[1].trade_id.ends_with("-2"));
    assert(trades[1].taker_order_id == 3);
    assert(trades[1].maker_order_id == 2);
    assert(trades[1].quantity == 3);

    assert(engine.book().best_ask()->id == 2);
    assert(engine.book().best_ask()->remaining_quantity() == 2);

    return 0;
}
