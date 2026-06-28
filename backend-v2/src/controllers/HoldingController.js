const HoldingService = require("../services/HoldingService");

class HoldingController {
  async getHoldings(req, res) {
    try {
      const holdings =
        await HoldingService.getHoldings(
          req.user.userId
        );

      res.status(200).json(holdings);
    } catch (error) {
      res.status(400).json({
        error: error.message,
      });
    }
  }
}

module.exports = new HoldingController();
