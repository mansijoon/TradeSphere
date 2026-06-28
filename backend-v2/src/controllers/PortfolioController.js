const PortfolioService = require("../services/PortfolioService");

class PortfolioController {
  async getPortfolio(req, res) {
    try {
      const portfolio =
        await PortfolioService.getPortfolio(
          req.user.userId
        );

      res.status(200).json(portfolio);
    } catch (error) {
      res.status(400).json({
        error: error.message,
      });
    }
  }
}

module.exports = new PortfolioController();
