import { Server as HttpServer } from "node:http";
import { WebSocketServer, WebSocket } from "ws";

type Client = {
  socket: WebSocket;
  subscriptions: Set<string>;
};

type ClientMessage =
  | {
      type: "subscribe";
      channel: string;
    }
  | {
      type: "unsubscribe";
      channel: string;
    };

export class WebSocketServerManager {
  private readonly wss: WebSocketServer;
  private readonly clients = new Set<Client>();

  constructor(server: HttpServer) {
    this.wss = new WebSocketServer({
      server,
      path: "/ws",
    });

    this.wss.on("connection", (socket) => {
      const client: Client = {
        socket,
        subscriptions: new Set(),
      };

      this.clients.add(client);

      this.send(socket, {
        type: "connection.established",
        timestamp: new Date().toISOString(),
      });

      socket.on("message", (data) => {
        this.handleMessage(client, data.toString());
      });

      socket.on("close", () => {
        this.clients.delete(client);
      });

      socket.on("error", () => {
        this.clients.delete(client);
      });
    });
  }

  private handleMessage(client: Client, rawMessage: string): void {
    try {
      const message = JSON.parse(rawMessage) as ClientMessage;

      if (
        message.type !== "subscribe" &&
        message.type !== "unsubscribe"
      ) {
        this.send(client.socket, {
          type: "error",
          message: "Unsupported message type",
        });
        return;
      }

      if (
        typeof message.channel !== "string" ||
        message.channel.length === 0
      ) {
        this.send(client.socket, {
          type: "error",
          message: "Channel is required",
        });
        return;
      }

      if (message.type === "subscribe") {
        client.subscriptions.add(message.channel);

        this.send(client.socket, {
          type: "subscription.confirmed",
          channel: message.channel,
        });

        return;
      }

      client.subscriptions.delete(message.channel);

      this.send(client.socket, {
        type: "unsubscription.confirmed",
        channel: message.channel,
      });
    } catch {
      this.send(client.socket, {
        type: "error",
        message: "Invalid JSON message",
      });
    }
  }

  broadcastToChannel(channel: string, event: unknown): void {
    for (const client of this.clients) {
      if (
        client.subscriptions.has(channel) &&
        client.socket.readyState === WebSocket.OPEN
      ) {
        this.send(client.socket, event);
      }
    }
  }

  broadcast(event: unknown): void {
    for (const client of this.clients) {
      if (client.socket.readyState === WebSocket.OPEN) {
        this.send(client.socket, event);
      }
    }
  }

  get connectionCount(): number {
    return this.clients.size;
  }

  close(): void {
    for (const client of this.clients) {
      client.socket.close();
    }

    this.clients.clear();
    this.wss.close();
  }

  private send(socket: WebSocket, event: unknown): void {
    if (socket.readyState === WebSocket.OPEN) {
      socket.send(JSON.stringify(event));
    }
  }
}
