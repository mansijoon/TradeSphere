require("dotenv").config();

// ✅ ALL EVENT HANDLERS MUST LOAD FIRST
require("./src/events/handlers/buyHandler");
require("./src/events/handlers/sellHandler");
require("./src/events/handlers/cacheHandler");
require("./src/events/handlers/tradeHandler");
require("./src/events/handlers/tradeSettlementHandler");
require("./src/events/handlers/portfolioSyncHandler");

const http = require("http");

const app = require("./src/app");
const connectDatabase = require("./src/config/database");
const startMarketSocket = require("./src/websocket/marketSocket");

const PORT = process.env.PORT || 5000;

async function startServer() {
  await connectDatabase();
  console.log("Database connected");

  const server = http.createServer(app);

  startMarketSocket(server);

  server.listen(PORT, () => {
    console.log(`TradeSphere API running on port ${PORT}`);
  });
}

startServer().catch((err) => {
  console.error("Server failed to start:", err);
  process.exit(1);
});
