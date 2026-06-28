const MarketPriceRepository =
  require("../repositories/MarketPriceRepository");

class MarketDataService {
  async getPrice(symbol) {
    const marketPrice =
      await MarketPriceRepository.findBySymbol(
        symbol
      );

    if (!marketPrice) {
      return 0;
    }

    return marketPrice.price;
  }

  async getAllPrices() {
    return MarketPriceRepository.findAll();
  }

  async updatePrice(
    symbol,
    price
  ) {
    return MarketPriceRepository.updatePrice(
      symbol,
      price
    );
  }
}

module.exports =
  new MarketDataService();
