const eventBus = require("../eventBus");
const EVENT = require("../types");

const HoldingService = require("../../services/HoldingService");
const TradeStatsService = require("../../services/TradeStatsService");

eventBus.on(EVENT.TRADE_EXECUTED, async (trade) => {
  try {
    const { buy, sell, quantity, price } = trade;

    // BUY side update
    await HoldingService.addHolding(
      buy.userId,
      buy.symbol,
      quantity,
      price
    );

    await TradeStatsService.recordBuy(
      buy.userId,
      quantity * price
    );

    // SELL side update
    await TradeStatsService.recordSell(
      sell.userId,
      quantity * price,
      (price - sell.price) * quantity
    );

  } catch (err) {
    console.error("TRADE handler error:", err);
  }
});
