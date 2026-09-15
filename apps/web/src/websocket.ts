export type TradeExecutedEvent = {
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

type ConnectionEvent = {
  type: "connection.established" | "subscription.confirmed";
  channel?: string;
  timestamp?: string;
};

export type MarketWebSocketEvent =
  | TradeExecutedEvent
  | ConnectionEvent;

export function createMarketWebSocket(
  channel: string,
  onEvent: (event: MarketWebSocketEvent) => void,
  onStatus?: (connected: boolean) => void,
): () => void {
  const url = import.meta.env.VITE_WS_URL ?? "ws://localhost:3000/ws";
  const socket = new WebSocket(url);

  socket.addEventListener("open", () => {
    onStatus?.(true);

    socket.send(
      JSON.stringify({
        type: "subscribe",
        channel,
      }),
    );
  });

  socket.addEventListener("message", (message) => {
    try {
      const event = JSON.parse(message.data) as MarketWebSocketEvent;
      onEvent(event);
    } catch {
      // Ignore malformed events from the edge.
    }
  });

  socket.addEventListener("close", () => {
    onStatus?.(false);
  });

  socket.addEventListener("error", () => {
    onStatus?.(false);
  });

  return () => {
    socket.close();
  };
}
