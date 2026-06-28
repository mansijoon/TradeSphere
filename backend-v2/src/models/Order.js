const mongoose = require("mongoose");

const orderSchema = new mongoose.Schema(
  {
    userId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "User",
      required: true,
    },

    symbol: {
      type: String,
      required: true,
    },

    originalQuantity: {
  type: Number,
  required: true,
},

filledQuantity: {
  type: Number,
  default: 0,
},

remainingQuantity: {
  type: Number,
  required: true,
},

    price: {
      type: Number,
      required: true,
    },

    side: {
      type: String,
      enum: ["BUY", "SELL"],
      required: true,
    },

    status: {
      type: String,
      enum: ["PENDING", "PARTIALLY_FILLED", "FILLED", "CANCELLED"],
      default: "PENDING",
    },
  },
  {
    timestamps: true,
  }
);

module.exports = mongoose.model(
  "Order",
  orderSchema
);
