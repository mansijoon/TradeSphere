const Holding = require("../models/Holding");

class HoldingRepository {
  async findByUserAndSymbol(userId, symbol) {
    return Holding.findOne({ userId, symbol });
  }

  async create(data) {
    return Holding.create(data);
  }

  async save(holding) {
    return holding.save();
  }

  async findByUserId(userId) {
    return Holding.find({ userId });
  }
}

module.exports = new HoldingRepository();
