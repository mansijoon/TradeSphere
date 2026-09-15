import "dotenv/config";
import { createServer } from "node:http";
import { app } from "./app.js";
import { WebSocketServerManager } from "./websocket/websocket-server.js";
import { KafkaTradeConsumer } from "./websocket/kafka-trade-consumer.js";

const PORT = Number(process.env.PORT ?? 3000);

const server = createServer(app);
const websocketServer = new WebSocketServerManager(server);
const kafkaTradeConsumer = new KafkaTradeConsumer(websocketServer);

server.listen(PORT, async () => {
  console.log(`API Gateway listening on port ${PORT}`);
  console.log(`WebSocket server listening on ws://localhost:${PORT}/ws`);

  try {
    await kafkaTradeConsumer.start();
  } catch (error) {
    console.error("Kafka consumer failed to start:", error);
  }
});

const shutdown = async () => {
  await kafkaTradeConsumer.stop().catch(() => undefined);

  websocketServer.close();

  server.close(() => {
    process.exit(0);
  });
};

process.on("SIGTERM", shutdown);
process.on("SIGINT", shutdown);
