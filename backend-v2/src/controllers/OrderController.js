const OrderService = require("../services/OrderService");

function parseNumber(value) {
  const n = Number(value);
  return Number.isFinite(n) ? n : null;
}

class OrderController {
  async placeBuyOrder(req, res) {
    try {
      const { symbol, quantity, price } = req.body;

      const order = await OrderService.placeBuyOrder({
        userId: req.user.userId,
        symbol,
        quantity: parseNumber(quantity),
        price: parseNumber(price),
      });

      res.status(201).json(order);
    } catch (error) {
      res.status(400).json({ error: error.message });
    }
  }

  async placeSellOrder(req, res) {
    try {
      const { symbol, quantity, price } = req.body;

      const order = await OrderService.placeSellOrder({
        userId: req.user.userId,
        symbol,
        quantity: parseNumber(quantity),
        price: parseNumber(price),
      });

      res.status(201).json(order);
    } catch (error) {
      res.status(400).json({ error: error.message });
    }
  }

  async getOrders(req, res) {
    try {
      const orders = await OrderService.getOrders(req.user.userId);
      res.status(200).json(orders);
    } catch (error) {
      res.status(400).json({ error: error.message });
    }
  }

  async getOpenOrders(req, res) {
    try {
      const book =
        await OrderService.getOpenOrders(
          req.params.symbol
        );

      res.status(200).json(book);
    } catch (error) {
      res.status(400).json({
        error: error.message
      });
    }
  }

  async getMarketDepth(req, res) {
    try {
      const depth =
        await OrderService.getMarketDepth(
          req.params.symbol
        );

      res.status(200).json(depth);
    } catch (error) {
      res.status(400).json({
        error: error.message
      });
    }
}

  async cancelOrder(req, res) {
    try {
      const order =
        await OrderService.cancelOrder(
          req.params.orderId
        );

      res.status(200).json(order);
    } catch (error) {
      res.status(400).json({
        error: error.message
      });
    }
  }
}


module.exports = new OrderController();
