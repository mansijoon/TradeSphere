const TradeStatsRepository =
  require("../repositories/TradeStatsRepository");

class TradeStatsService {
  async getOrCreateStats(
    userId,
    session = null
  ) {
    let stats =
      await TradeStatsRepository.findByUserId(
        userId,
        session
      );

    if (!stats) {
      stats =
        await TradeStatsRepository.create(
          {
            userId,
          },
          session
        );
    }

    return stats;
  }

  async recordBuy(
    userId,
    amount,
    session = null
  ) {
    const stats =
      await this.getOrCreateStats(
        userId,
        session
      );

    stats.totalTrades += 1;
    stats.totalBuyValue += amount;

    return TradeStatsRepository.save(
      stats,
      session
    );
  }

  async recordSell(
    userId,
    amount,
    realizedProfit,
    session = null
  ) {
    const stats =
      await this.getOrCreateStats(
        userId,
        session
      );

    stats.totalTrades += 1;
    stats.totalSellValue += amount;
    stats.realizedPnL +=
      realizedProfit;

    return TradeStatsRepository.save(
      stats,
      session
    );
  }

  async getStats(userId) {
    return TradeStatsRepository.findByUserId(
      userId
    );
  }
}

module.exports =
  new TradeStatsService();
