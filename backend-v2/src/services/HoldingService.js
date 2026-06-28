const HoldingRepository = require("../repositories/HoldingRepository");

class HoldingService {
  async addHolding(userId, symbol, quantity, price) {
    const Holding = require("../models/Holding");

    const existing = await Holding.findOne({ userId, symbol });

    if (!existing) {
      await Holding.create({
        userId,
        symbol,
        quantity,
        averagePrice: price,
      });
      return;
    }

    const newQty = existing.quantity + quantity;

    const newAvg =
      (existing.quantity * existing.averagePrice +
        quantity * price) /
      newQty;

    await Holding.updateOne(
      { userId, symbol },
      {
        $set: {
          quantity: newQty,
          averagePrice: newAvg,
        },
      }
    );
  }

  async removeHolding(userId, symbol, quantity) {
    const Holding = require("../models/Holding");

    const holding = await Holding.findOne({ userId, symbol });

    if (!holding) throw new Error("Holding not found");
    if (holding.quantity < quantity) throw new Error("Insufficient shares");

    const avg = holding.averagePrice;

    holding.quantity -= quantity;

    await holding.save();

    return { quantitySold: quantity, averagePrice: avg };
  }

  async getHoldings(userId) {
    const Holding = require("../models/Holding");
    return Holding.find({ userId });
  }
}

module.exports = new HoldingService();
