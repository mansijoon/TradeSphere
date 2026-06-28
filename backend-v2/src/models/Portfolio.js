const mongoose = require("mongoose");

const portfolioSchema = new mongoose.Schema(
  {
    userId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "User",
      required: true,
    },

    cashBalance: {
      type: Number,
      required: true,
      default: 100000,
    },
  },
  {
    timestamps: true,
  }
);

module.exports = mongoose.model(
  "Portfolio",
  portfolioSchema
);
