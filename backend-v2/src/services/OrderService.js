const mongoose = require("mongoose");

const OrderRepository = require("../repositories/OrderRepository");
const PortfolioRepository = require("../repositories/PortfolioRepository");
const TransactionService = require("./TransactionService");

const HoldingService = require("./HoldingService");
const TradeStatsService = require("./TradeStatsService");

const HoldingRepository = require("../repositories/HoldingRepository");
const MarketPriceRepository = require("../repositories/MarketPriceRepository");

const eventBus = require("../events/eventBus");
const EVENT = require("../events/types");
const { processOrder } = require("../orderbook/matchEngine");

const PortfolioSummaryService = require("./PortfolioSummaryService");

async function buildSnapshot(userId) {
  const portfolio = await PortfolioRepository.findByUserId(userId);
  const holdings = await HoldingRepository.findByUserId(userId);

  let invested = 0;
  let market = 0;

  for (const h of holdings) {
    const p = await MarketPriceRepository.findBySymbol(h.symbol);
    const price = p?.price || 0;

    invested += h.quantity * h.averagePrice;
    market += h.quantity * price;
  }

  return {
    cashBalance: portfolio.cashBalance,
    investedValue: invested,
    marketValue: market,
    holdings
  };
}

class OrderService {

  async placeBuyOrder({ userId, symbol, quantity, price }) {
    const session = await mongoose.startSession();

    try {
      session.startTransaction();

      const portfolio = await PortfolioRepository.findByUserId(userId);
      if (!portfolio) throw new Error("Portfolio not found");

      const cost = quantity * price;

      if (portfolio.cashBalance < cost) {
        throw new Error("Insufficient funds");
      }

      await PortfolioRepository.updateCashBalance(
        userId,
        portfolio.cashBalance - cost,
        session
      );

      const order = await OrderRepository.create(
        {
          userId,
          symbol,
          originalQuantity: quantity,
          filledQuantity: 0,
          remainingQuantity: quantity,
          price,
          side: "BUY",
          status: "PENDING"
        },
        session
      );
      processOrder(order);


      await TransactionService.createTransaction(
        { userId, type: "BUY", symbol, quantity, amount: cost },
        session
      );
      

      await session.commitTransaction();
      session.endSession();

      // 🔥 IMPORTANT: emit FULL snapshot
      const snapshot =
  await PortfolioSummaryService.getSummary(
    userId
  );

      eventBus.emit(EVENT.PORTFOLIO_UPDATE, {
        userId,
        snapshot
      });

      return order;

    } catch (e) {
      await session.abortTransaction().catch(() => {});
      session.endSession();
      throw e;
    }
  }

  async placeSellOrder({ userId, symbol, quantity, price }) {
    const session = await mongoose.startSession();

    try {
      session.startTransaction();

      const portfolio = await PortfolioRepository.findByUserId(userId);
      if (!portfolio) throw new Error("Portfolio not found");

      const revenue = quantity * price;

      await PortfolioRepository.updateCashBalance(
        userId,
        portfolio.cashBalance + revenue,
        session
      );

      const order = await OrderRepository.create(
        {
          userId,
          symbol,
          originalQuantity: quantity,
          filledQuantity: 0,
          remainingQuantity: quantity,
          price,
          side: "SELL",
          status: "PENDING"
        },
        session

      );

      processOrder(order);

      await TransactionService.createTransaction(
        { userId, type: "SELL", symbol, quantity, amount: revenue },
        session
      );
      
      await session.commitTransaction();
      session.endSession();

      const snapshot =
  await PortfolioSummaryService.getSummary(
    userId
  );

      eventBus.emit(EVENT.PORTFOLIO_UPDATE, {
        userId,
        snapshot
      });

      return order;

    } catch (e) {
      await session.abortTransaction().catch(() => {});
      session.endSession();
      throw e;
    }
  }
  
    async cancelOrder(orderId) {
    const order =
      await OrderRepository.cancelOrder(
        orderId
      );

    if (!order) {
      throw new Error(
        "Order not found or already closed"
      );
    }

    const orderBook =
      require("../orderbook/orderBook");

    orderBook.removeOrder(orderId);

    return order;
  }

  async getOrders(userId) {
    return OrderRepository.findByUserId(userId);
  }

  

  async getOpenOrders(symbol) {
    const orders =
      await OrderRepository.findPendingBySymbol(symbol);

    return {
      bids: orders
        .filter(o => o.side === "BUY")
        .sort((a,b) => b.price - a.price),

      asks: orders
        .filter(o => o.side === "SELL")
        .sort((a,b) => a.price - b.price)
    };
  }

  async getMarketDepth(symbol) {
    const orders =
      await OrderRepository.findPendingBySymbol(symbol);

    const bidMap = {};
    const askMap = {};

    for (const o of orders) {
      const key = Number(o.price).toFixed(2);

      if (o.side === "BUY") {
        bidMap[key] =
          (bidMap[key] || 0) +
          o.remainingQuantity;
      } else {
        askMap[key] =
          (askMap[key] || 0) +
          o.remainingQuantity;
      }
    }

    return {
      bids: Object.entries(bidMap)
        .map(([price, quantity]) => ({
          price: Number(price),
          quantity
        }))
        .sort((a,b) => b.price - a.price),

      asks: Object.entries(askMap)
        .map(([price, quantity]) => ({
          price: Number(price),
          quantity
        }))
        .sort((a,b) => a.price - b.price)
    };
  }
}

module.exports = new OrderService();
