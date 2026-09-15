import { Kafka, Consumer, EachMessagePayload } from "kafkajs";
import { WebSocketServerManager } from "./websocket-server.js";

type TradeExecutedEvent = {
  eventType: "TradeExecuted";
  eventVersion: number;
  tradeId: string;
  takerOrderId: string;
  makerOrderId: string;
  instrumentId: string;
  price: number;
  quantity: number;
  timestamp: string;
};

export class KafkaTradeConsumer {
  private readonly consumer: Consumer;

  constructor(
    private readonly websocketServer: WebSocketServerManager,
  ) {
    const brokers = (
      process.env.KAFKA_BROKERS ?? "localhost:31092"
    ).split(",");

    const kafka = new Kafka({
      clientId: "marketforge-api-gateway",
      brokers,
      ssl: process.env.KAFKA_SECURITY_PROTOCOL === "SSL"
        ? {
            rejectUnauthorized: false,
          }
        : undefined,
    });

    this.consumer = kafka.consumer({
      groupId:
        process.env.KAFKA_CONSUMER_GROUP ??
        "marketforge-api-gateway",
    });
  }

  async start(): Promise<void> {
    await this.consumer.connect();

    await this.consumer.subscribe({
      topic: process.env.KAFKA_TRADE_TOPIC ?? "trade.executed",
      fromBeginning: false,
    });

    await this.consumer.run({
      eachMessage: async (payload) => {
        await this.handleMessage(payload);
      },
    });

    console.log("Kafka trade consumer started");
  }

  async stop(): Promise<void> {
    await this.consumer.disconnect();
  }

  private async handleMessage({
    message,
  }: EachMessagePayload): Promise<void> {
    if (!message.value) {
      return;
    }

    try {
      const event = JSON.parse(
        message.value.toString(),
      ) as TradeExecutedEvent;

      if (
        event.eventType !== "TradeExecuted" ||
        !event.tradeId ||
        !event.instrumentId
      ) {
        console.error("Invalid TradeExecuted event");
        return;
      }

      this.websocketServer.broadcastToChannel("trades", event);

      this.websocketServer.broadcastToChannel(
        `market:${event.instrumentId}`,
        event,
      );
    } catch (error) {
      console.error("Failed to process Kafka trade event:", error);
    }
  }
}
