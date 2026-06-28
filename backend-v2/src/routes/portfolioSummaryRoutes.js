const express = require("express");

const PortfolioSummaryController =
  require("../controllers/PortfolioSummaryController");

const authMiddleware =
  require("../middleware/authMiddleware");

const router = express.Router();

router.get(
  "/me",
  authMiddleware,
  (req, res) =>
    PortfolioSummaryController.getSummary(
      req,
      res
    )
);

module.exports = router;
