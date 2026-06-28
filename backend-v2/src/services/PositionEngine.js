const HoldingRepository = require("../repositories/HoldingRepository");

/**
 * SINGLE SOURCE OF TRUTH FOR POSITIONS
 * BUY  -> increases holding + recalculates avg price
 * SELL -> decreases holding + validates quantity
 */
async function applyTrade({ userId, symbol, quantity, price, side }) {
  let holding = await HoldingRepository.findOne(userId, symbol);

  // create empty holding if not exists
  if (!holding) {
    holding = {
      userId,
      symbol,
      quantity: 0,
      averagePrice: 0,
    };
  }

  // ---------------- BUY ----------------
  if (side === "BUY") {
    const totalCost =
      holding.quantity * holding.averagePrice + quantity * price;

    const newQty = holding.quantity + quantity;

    holding.averagePrice = totalCost / newQty;
    holding.quantity = newQty;
  }

  // ---------------- SELL ----------------
  if (side === "SELL") {
    if (holding.quantity < quantity) {
      throw new Error("INSUFFICIENT_HOLDINGS");
    }

    holding.quantity -= quantity;

    if (holding.quantity === 0) {
      holding.averagePrice = 0;
    }
  }

  await HoldingRepository.upsert(userId, symbol, holding);

  return holding;
}

module.exports = {
  applyTrade,
};
