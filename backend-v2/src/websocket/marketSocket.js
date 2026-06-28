const WebSocket = require("ws");
const MarketPriceRepository = require("../repositories/MarketPriceRepository");
const HoldingRepository = require("../repositories/HoldingRepository");
const PortfolioRepository = require("../repositories/PortfolioRepository");
const TradeStatsRepository = require("../repositories/TradeStatsRepository");
const PortfolioSummaryService = require("../services/PortfolioSummaryService");
const eventBus = require("../events/eventBus");
const EVENT = require("../events/types");

function startMarketSocket(server) {
  const wss = new WebSocket.Server({ server });

  async function computePortfolio(userId) {
    const portfolio = await PortfolioRepository.findByUserId(userId);
    const holdings = await HoldingRepository.findByUserId(userId) || [];
    const tradeStats = await TradeStatsRepository.findByUserId(userId);
const PortfolioSummaryService = require("../services/PortfolioSummaryService");

    let invested = 0;
    let market = 0;

    for (const h of holdings) {
      const priceData = await MarketPriceRepository.findBySymbol(h.symbol);
      const price = priceData?.price || 0;

      invested += h.quantity * h.averagePrice;
      market += h.quantity * price;
    }

    return {
      cashBalance: portfolio?.cashBalance || 0,
      investedValue: invested,
      marketValue: market,
      unrealizedPnL: market - invested,
      realizedPnL: tradeStats?.realizedPnL || 0,
      totalPnL: (market - invested) + (tradeStats?.realizedPnL || 0),
      totalPortfolioValue: (portfolio?.cashBalance || 0) + market,
      holdings
    };
  }

  // store connections
  const clients = new Set();

  wss.on("connection", (ws) => {
    clients.add(ws);

    ws.on("message", async (msg) => {
      const data = JSON.parse(msg);

      if (data.type === "subscribe_portfolio") {
        ws.userId = data.userId;

        const snapshot = await PortfolioSummaryService.getSummary(ws.userId);

        ws.send(JSON.stringify({
          type: "portfolio_snapshot",
          data: snapshot
        }));
      }
    });

    ws.on("close", () => {
      clients.delete(ws);
    });
  });

  // PRICE STREAM
  const symbols = ["AAPL", "GOOGL", "MSFT", "NVDA", "TSLA"];

  setInterval(async () => {
    const volatilityMap = {
      AAPL: 0.8,
      MSFT: 1.2,
      GOOGL: 1.0,
      TSLA: 2.5,
      NVDA: 2.0,
    };

    for (const symbol of symbols) {
      const data = await MarketPriceRepository.findBySymbol(symbol);
      if (!data) continue;

      const change = Math.sin(Date.now() / 10000) * (volatilityMap[symbol] || 1);
      const newPrice = Math.max(1, data.price + change);

      await MarketPriceRepository.updatePrice(symbol, newPrice);

      for (const c of clients) {
        if (c.readyState === WebSocket.OPEN) {
          c.send(JSON.stringify({
            type: "price_update",
            symbol,
            price: newPrice
          }));
        }
      }
    }
  }, 2000);

  // 🔥 CRITICAL FIX: PUSH PORTFOLIO AFTER TRADE
  eventBus.on(EVENT.PORTFOLIO_UPDATE, async (payload) => {
    try {
      const userId = payload.userId;
      if (!userId) return;

      const snapshot = payload.snapshot || payload.data;
      if (!snapshot) return;

      for (const c of clients) {
        if (c.readyState === WebSocket.OPEN && c.userId == userId) {
          c.send(JSON.stringify({
            type: "portfolio_update",
            data: snapshot
          }));
        }
      }
    } catch (e) {
      console.log("portfolio push error", e);
    }
  });

  return wss;
}

module.exports = startMarketSocket;
