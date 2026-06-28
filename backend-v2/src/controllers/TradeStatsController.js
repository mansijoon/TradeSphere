const TradeStatsService =
  require("../services/TradeStatsService");

class TradeStatsController {
  async getStats(req, res) {
    try {
      const stats =
        await TradeStatsService.getStats(
          req.user.userId
        );

      res.status(200).json(stats);
    } catch (error) {
      res.status(400).json({
        error: error.message,
      });
    }
  }
}

module.exports =
  new TradeStatsController();
