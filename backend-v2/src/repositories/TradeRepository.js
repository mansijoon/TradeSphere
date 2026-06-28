const Trade = require("../models/Trade");

class TradeRepository {

  async create(data) {
    const trade = new Trade(data);
    return trade.save();
  }

  async findByUser(userId) {
    return Trade.find({
      $or: [
        { buyerId: userId },
        { sellerId: userId }
      ]
    }).sort({
      createdAt: -1
    });
  }

  async findBySymbol(symbol) {
    return Trade.find({
      symbol
    }).sort({
      createdAt: -1
    });
  }
}

module.exports =
  new TradeRepository();
