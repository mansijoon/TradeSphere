const MarketPrice =
  require("../models/MarketPrice");

class MarketPriceRepository {
  async findBySymbol(symbol) {
    return MarketPrice.findOne({
      symbol,
    });
  }

  async findAll() {
    return MarketPrice.find();
  }

  async create(data) {
    return MarketPrice.create(data);
  }

  async updatePrice(
    symbol,
    price
  ) {
    return MarketPrice.findOneAndUpdate(
      { symbol },
      { price },
      {
        new: true,
        upsert: true,
      }
    );
  }
}

module.exports =
  new MarketPriceRepository();
