const eventBus = require("../eventBus");
const EVENT = require("../types");

const HoldingService = require("../../services/HoldingService");
const TradeStatsService = require("../../services/TradeStatsService");
const EventLogService = require("../../services/EventLogService");

eventBus.on(EVENT.ORDER_BUY, async (data) => {
  try {
    await HoldingService.addHolding(
      data.userId,
      data.symbol,
      data.quantity,
      data.price
    );

    await TradeStatsService.recordBuy(
      data.userId,
      data.totalCost
    );

    // NEW: audit log
    await EventLogService.logEvent(
      data.userId,
      EVENT.ORDER_BUY,
      data
    );

  } catch (err) {
    console.error("BUY handler error:", err);
  }
});
