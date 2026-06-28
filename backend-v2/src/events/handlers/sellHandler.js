const eventBus = require("../eventBus");
const EVENT = require("../types");

const TradeStatsService = require("../../services/TradeStatsService");
const EventLogService = require("../../services/EventLogService");

eventBus.on(EVENT.ORDER_SELL, async (data) => {
  try {
    await TradeStatsService.recordSell(
      data.userId,
      data.saleValue,
      data.realizedProfit
    );

    // NEW: audit log
    await EventLogService.logEvent(
      data.userId,
      EVENT.ORDER_SELL,
      data
    );

  } catch (err) {
    console.error("SELL handler error:", err);
  }
});
