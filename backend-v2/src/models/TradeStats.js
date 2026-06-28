const mongoose = require("mongoose");

const tradeStatsSchema = new mongoose.Schema(
  {
    userId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "User",
      required: true,
      unique: true,
    },

    realizedPnL: {
      type: Number,
      default: 0,
    },

    totalBuyValue: {
      type: Number,
      default: 0,
    },

    totalSellValue: {
      type: Number,
      default: 0,
    },

    totalTrades: {
      type: Number,
      default: 0,
    },
  },
  {
    timestamps: true,
  }
);

module.exports = mongoose.model(
  "TradeStats",
  tradeStatsSchema
);
