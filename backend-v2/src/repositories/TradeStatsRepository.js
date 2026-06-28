const TradeStats = require("../models/TradeStats");

class TradeStatsRepository {
  async findByUserId(
    userId,
    session = null
  ) {
    return TradeStats.findOne({
      userId,
    }).session(session);
  }

  async create(
    data,
    session = null
  ) {
    const stats =
      new TradeStats(data);

    return stats.save({
      session,
    });
  }

  async save(
    stats,
    session = null
  ) {
    return stats.save({
      session,
    });
  }
}

module.exports =
  new TradeStatsRepository();
