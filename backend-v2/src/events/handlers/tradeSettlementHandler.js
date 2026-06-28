const eventBus = require("../eventBus");
const EVENT = require("../types");

const PortfolioRepository = require("../../repositories/PortfolioRepository");
const OrderRepository = require("../../repositories/OrderRepository");
const HoldingService = require("../../services/HoldingService");
const TradeStatsService = require("../../services/TradeStatsService");
const TradeService = require("../../services/TradeService");

eventBus.on(EVENT.TRADE_EXECUTED, async (trade) => {
  try {
    const { buy, sell, quantity, price } = trade;

    await TradeService.recordTrade(trade);

    await OrderRepository.updateFill(buy._id, quantity);
    await OrderRepository.updateFill(sell._id, quantity);

    // BUY SIDE → holdings
    await HoldingService.addHolding(
      buy.userId,
      buy.symbol,
      quantity,
      price
    );

    // SELL SIDE → holdings reduction + cash effect handled here
    await HoldingService.removeHolding(
      sell.userId,
      sell.symbol,
      quantity
    );

    // STATS UPDATE
    await TradeStatsService.recordBuy(
      buy.userId,
      quantity * price
    );

    await TradeStatsService.recordSell(
      sell.userId,
      quantity * price,
      (price - sell.price) * quantity
    );

  } catch (err) {
    console.error("TRADE SETTLEMENT ERROR:", err);
  }
});
