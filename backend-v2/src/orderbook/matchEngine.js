const orderBook = require("./orderBook");
const eventBus = require("../events/eventBus");
const EVENT = require("../events/types");

function processOrder(order) {
  if (!order || !order.symbol || !order.price) return;

  orderBook.addOrder(order);

  const trades = orderBook.match();

  for (const trade of trades) {
    eventBus.emit(EVENT.TRADE_EXECUTED, {
      buy: trade.buy,
      sell: trade.sell,
      quantity: trade.quantity,
      price: trade.price,
    });
  }
}

module.exports = { processOrder };
