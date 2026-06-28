const TradeRepository =
  require("../repositories/TradeRepository");

class TradeService {

  async recordTrade(trade) {
    return TradeRepository.create({
      symbol: trade.buy.symbol,

      buyerId:
        trade.buy.userId,

      sellerId:
        trade.sell.userId,

      buyOrderId:
        trade.buy._id,

      sellOrderId:
        trade.sell._id,

      quantity:
        trade.quantity,

      price:
        trade.price
    });
  }

  async getUserTrades(userId) {
    return TradeRepository.findByUser(
      userId
    );
  }
}

module.exports =
  new TradeService();
