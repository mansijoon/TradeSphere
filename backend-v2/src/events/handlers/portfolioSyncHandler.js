const eventBus = require("../eventBus");
const EVENT = require("../types");

const HoldingRepository = require("../../repositories/HoldingRepository");
const PortfolioRepository = require("../../repositories/PortfolioRepository");
const MarketPriceRepository = require("../../repositories/MarketPriceRepository");

async function computePortfolio(userId) {
  const portfolio = await PortfolioRepository.findByUserId(userId);
  const holdings = await HoldingRepository.findByUserId(userId);

  let invested = 0;
  let market = 0;

  for (const h of holdings) {
    const price = await MarketPriceRepository.findBySymbol(h.symbol);
    const p = price?.price || 0;

    invested += h.quantity * h.averagePrice;
    market += h.quantity * p;
  }

  return {
    cashBalance: portfolio.cashBalance,
    investedValue: invested,
    marketValue: market,
    unrealizedPnL: market - invested,
    holdings
  };
}

// SINGLE SOURCE OF TRUTH
eventBus.on(EVENT.TRADE_EXECUTED, async (trade) => {
  try {
    const userId = trade.buy?.userId || trade.sell?.userId;
    if (!userId) return;

    const snapshot = await computePortfolio(userId);

    eventBus.emit(EVENT.PORTFOLIO_UPDATE, {
      type: "portfolio_update",
      userId,
      data: snapshot
    });

  } catch (err) {
    console.error("portfolio sync error:", err);
  }
});

module.exports = {};
