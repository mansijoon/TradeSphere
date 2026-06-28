const Portfolio = require("../models/Portfolio");

class PortfolioRepository {
  async create(portfolioData, session = null) {
    return Portfolio.create(
      [portfolioData],
      { session }
    );
  }

  async findByUserId(userId) {
    return Portfolio.findOne({ userId });
  }

  async updateCashBalance(
    userId,
    cashBalance,
    session = null
  ) {
    return Portfolio.findOneAndUpdate(
      { userId },
      { cashBalance },
      {
        new: true,
        session,
      }
    );
  }
}

module.exports =
  new PortfolioRepository();
