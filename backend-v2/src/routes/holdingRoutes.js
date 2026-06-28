const express = require("express");

const HoldingController =
  require("../controllers/HoldingController");

const authMiddleware =
  require("../middleware/authMiddleware");

const router = express.Router();

router.get(
  "/me",
  authMiddleware,
  (req, res) =>
    HoldingController.getHoldings(
      req,
      res
    )
);

module.exports = router;
