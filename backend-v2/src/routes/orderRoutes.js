const express = require("express");

const OrderController = require("../controllers/OrderController");
const authMiddleware = require("../middleware/authMiddleware");

const router = express.Router();

router.post(
  "/buy",
  authMiddleware,
  (req, res) =>
    OrderController.placeBuyOrder(
      req,
      res
    )
);

router.post(
  "/sell",
  authMiddleware,
  (req, res) =>
    OrderController.placeSellOrder(
      req,
      res
    )
);

router.get(
  "/me",
  authMiddleware,
  (req, res) =>
    OrderController.getOrders(
      req,
      res
    )
);


router.get(
  "/open/:symbol",
  authMiddleware,
  (req, res) =>
    OrderController.getOpenOrders(
      req,
      res
    )
);


router.get(
  "/depth/:symbol",
  authMiddleware,
  (req, res) =>
    OrderController.getMarketDepth(
      req,
      res
    )
);

router.delete(
  "/:orderId",
  authMiddleware,
  (req, res) =>
    OrderController.cancelOrder(
      req,
      res
    )
);

module.exports = router;
