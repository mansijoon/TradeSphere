const express = require("express");

const TransactionController =
  require("../controllers/TransactionController");

const authMiddleware =
  require("../middleware/authMiddleware");

const router = express.Router();

router.get(
  "/me",
  authMiddleware,
  (req, res) =>
    TransactionController.getTransactions(
      req,
      res
    )
);

module.exports = router;
