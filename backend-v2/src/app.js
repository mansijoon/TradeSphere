const express = require("express");
const cors = require("cors");

const authRoutes = require("./routes/authRoutes");
const orderRoutes = require("./routes/orderRoutes");
const portfolioRoutes = require("./routes/portfolioRoutes");
const transactionRoutes = require("./routes/transactionRoutes");
const holdingRoutes = require("./routes/holdingRoutes");
const portfolioSummaryRoutes = require("./routes/portfolioSummaryRoutes");
const tradeStatsRoutes =
  require("./routes/tradeStatsRoutes");
  
const eventLogRoutes = require("./routes/eventLogRoutes");

const app = express();

app.use(cors());
app.use(express.json());

app.use("/api/auth", authRoutes);
app.use("/api/orders", orderRoutes);
app.use("/api/portfolios", portfolioRoutes);
app.use("/api/transactions", transactionRoutes);
app.use("/api/holdings", holdingRoutes);
app.use("/api/portfolio-summary", portfolioSummaryRoutes);
app.use("/api/event-logs", eventLogRoutes);
app.use(
  "/api/trade-stats",
  tradeStatsRoutes
);

module.exports = app;
