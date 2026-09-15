#include "marketforge/trade_id_generator.hpp"

#include <cassert>
#include <string>

int main() {
    marketforge::TradeIdGenerator first;
    marketforge::TradeIdGenerator second;

    const auto first_id = first.next();
    const auto second_id = first.next();
    const auto restarted_id = second.next();

    assert(first_id != second_id);
    assert(first_id != restarted_id);
    assert(second_id != restarted_id);

    assert(first_id.ends_with("-1"));
    assert(second_id.ends_with("-2"));
    assert(restarted_id.ends_with("-1"));

    return 0;
}
