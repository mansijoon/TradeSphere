const express = require("express");
const router = express.Router();

const EventLogService = require("../services/EventLogService");
const authMiddleware = require("../middleware/authMiddleware");

// GET user event logs
router.get("/me", authMiddleware, async (req, res) => {
  try {
    const userId = req.user.userId;

    const logs = await EventLogService.getLogs(userId);

    res.json(logs);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

module.exports = router;
