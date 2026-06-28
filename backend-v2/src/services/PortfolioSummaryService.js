const PortfolioRepository = require("../repositories/PortfolioRepository");
const HoldingRepository = require("../repositories/HoldingRepository");
const MarketDataService = require("./MarketDataService");
const TradeStatsService = require("./TradeStatsService");
const redisClient = require("../cache/redisClient");

class PortfolioSummaryService {
  async getSummary(userId) {
    const cacheKey = `portfolio:${userId}`;

    // 1. CHECK CACHE
    const cached = await redisClient.get(cacheKey);
    if (cached) {
      return JSON.parse(cached);
    }

    // 2. DB FETCH
    const portfolio =
      await PortfolioRepository.findByUserId(userId);

    const holdings =
      await HoldingRepository.findByUserId(userId);

    const tradeStats =
      await TradeStatsService.getStats(userId);

    const holdingDetails = await Promise.all(
      holdings.map(async (holding) => {
        const currentPrice =
          await MarketDataService.getPrice(holding.symbol);

        const invested =
          holding.quantity * holding.averagePrice;

        const currentValue =
          holding.quantity * currentPrice;

        return {
          symbol: holding.symbol,
          quantity: holding.quantity,
          averagePrice: holding.averagePrice,
          currentPrice,
          invested,
          currentValue,
          profitLoss: currentValue - invested,
        };
      })
    );

    const investedValue = holdingDetails.reduce(
      (sum, h) => sum + h.invested,
      0
    );

    const marketValue = holdingDetails.reduce(
      (sum, h) => sum + h.currentValue,
      0
    );

    const result = {
      cashBalance: portfolio.cashBalance,

      investedValue,
      marketValue,

      unrealizedPnL: marketValue - investedValue,

      realizedPnL: tradeStats?.realizedPnL || 0,

      totalPnL:
        (marketValue - investedValue) +
        (tradeStats?.realizedPnL || 0),

      totalTrades: tradeStats?.totalTrades || 0,

      totalPortfolioValue:
        portfolio.cashBalance + marketValue,

      holdings: holdingDetails,
    };

    // 3. STORE CACHE (TTL = 30 sec)
    await redisClient.setEx(
      cacheKey,
      30,
      JSON.stringify(result)
    );

    return result;
  }
}

module.exports = new PortfolioSummaryService();
