const express = require("express");

const TradeStatsController =
  require("../controllers/TradeStatsController");

const authMiddleware =
  require("../middleware/authMiddleware");

const router = express.Router();

router.get(
  "/me",
  authMiddleware,
  (req, res) =>
    TradeStatsController.getStats(
      req,
      res
    )
);

module.exports = router;
