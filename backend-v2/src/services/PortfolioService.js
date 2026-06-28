const PortfolioRepository = require("../repositories/PortfolioRepository");

class PortfolioService {
  async createPortfolio(userId) {
    return PortfolioRepository.create({
      userId,
      cashBalance: 100000,
    });
  }

  async getPortfolio(userId) {
    return PortfolioRepository.findByUserId(
      userId
    );
  }

  async updateCashBalance(
    userId,
    cashBalance
  ) {
    return PortfolioRepository.updateCashBalance(
      userId,
      cashBalance
    );
  }
}

module.exports = new PortfolioService();
