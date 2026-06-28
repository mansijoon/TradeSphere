const PortfolioSummaryService = require("../services/PortfolioSummaryService");

class PortfolioSummaryController {
  async getSummary(req, res) {
    try {
      const summary =
        await PortfolioSummaryService.getSummary(
          req.user.userId
        );

      res.status(200).json(summary);
    } catch (error) {
      res.status(400).json({
        error: error.message,
      });
    }
  }
}

module.exports =
  new PortfolioSummaryController();
