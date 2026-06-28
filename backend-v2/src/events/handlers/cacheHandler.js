const eventBus = require("../eventBus");
const EVENT = require("../types");
const redisClient = require("../../cache/redisClient");

async function invalidate(userId) {
  await redisClient.del(`portfolio:${userId}`);
}

// BUY → invalidate cache
eventBus.on(EVENT.ORDER_BUY, async (data) => {
  await invalidate(data.userId);
});

// SELL → invalidate cache
eventBus.on(EVENT.ORDER_SELL, async (data) => {
  await invalidate(data.userId);
});
