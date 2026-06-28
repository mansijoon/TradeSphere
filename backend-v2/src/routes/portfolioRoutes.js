const express = require("express");

const PortfolioController = require("../controllers/PortfolioController");
const authMiddleware = require("../middleware/authMiddleware");

const router = express.Router();

router.get(
  "/me",
  authMiddleware,
  (req, res) =>
    PortfolioController.getPortfolio(
      req,
      res
    )
);

module.exports = router;
