const Order = require("../models/Order");

class OrderRepository {
  async create(
    orderData,
    session = null
  ) {
    const order =
      new Order(orderData);

    return order.save({
      session,
    });
  }

  async findByUserId(userId) {
    return Order.find({ userId }).sort({
      createdAt: -1,
    });
  }

  async findById(orderId) {
    return Order.findById(orderId);
  }

  async updateStatus(
    orderId,
    status
  ) {
    return Order.findByIdAndUpdate(
      orderId,
      { status },
      { new: true }
    );
  }

  async findPendingBySymbol(symbol) {
    return Order.find({
      symbol,
      status: {
        $in: ["PENDING", "PARTIALLY_FILLED"]
      }
    }).sort({
      createdAt: 1
    });
  }

  async updateFill(orderId, filledQty) {
    const order = await Order.findById(orderId);

    if (!order) return null;

    order.filledQuantity += filledQty;
    order.remainingQuantity -= filledQty;

    if (order.remainingQuantity <= 0) {
      order.remainingQuantity = 0;
      order.status = "FILLED";
    } else {
      order.status = "PARTIALLY_FILLED";
    }

    return order.save();
  }
  
  async cancelOrder(orderId) {
  return Order.findOneAndUpdate(
    {
      _id: orderId,
      status: {
        $in: ["PENDING", "PARTIALLY_FILLED"]
      }
    },
    {
      status: "CANCELLED"
    },
    {
      new: true
    }
  );
}
}

module.exports =
  new OrderRepository();
