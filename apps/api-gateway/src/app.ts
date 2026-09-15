import "dotenv/config";
import express, { NextFunction, Request, Response } from "express";
import cors from "cors";
import helmet from "helmet";
import { createProxyMiddleware } from "http-proxy-middleware";
import { z } from "zod";
import { randomUUID } from "node:crypto";
import { requireAuth } from "./middleware/auth.js";

const app = express();

app.disable("x-powered-by");

app.use((req: Request, res: Response, next: NextFunction) => {
  const requestId = req.header("x-request-id") ?? randomUUID();

  res.setHeader("x-request-id", requestId);
  res.locals.requestId = requestId;

  next();
});

app.use(helmet());
app.use(cors({
  origin: process.env.CORS_ORIGIN ?? "http://localhost:5173",
}));
app.use(express.json({ limit: "1mb" }));

const tradingServiceUrl =
  process.env.TRADING_SERVICE_URL ?? "http://localhost:8080";

const marketServiceUrl =
  process.env.MARKET_SERVICE_URL ?? "http://localhost:8082";

const createOrderSchema = z.object({
  accountId: z.string().regex(/^[0-9a-fA-F-]{36}$/),
  instrumentId: z.string().regex(/^[0-9a-fA-F-]{36}$/),
  clientOrderId: z.string().min(1).max(100),
  side: z.enum(["BUY", "SELL"]),
  orderType: z.enum(["MARKET", "LIMIT"]),
  timeInForce: z.enum(["DAY", "GTC", "IOC", "FOK"]),
  price: z.number().positive().optional(),
  quantity: z.number().positive(),
}).superRefine((order, ctx) => {
  if (order.orderType === "LIMIT" && order.price === undefined) {
    ctx.addIssue({
      code: "custom",
      path: ["price"],
      message: "Price is required for LIMIT orders",
    });
  }
});

app.post(
  "/api/v1/orders",
  requireAuth,
  (req: Request, res: Response, next: NextFunction) => {
    const result = createOrderSchema.safeParse(req.body);

    if (!result.success) {
      res.status(400).json({
        error: "VALIDATION_ERROR",
        message: "Invalid order request",
        details: result.error.issues.map((issue) => ({
          path: issue.path,
          message: issue.message,
        })),
      });
      return;
    }

    next();
  },
  requireAuth,
);

app.use(
  "/api/v1/orders",
  createProxyMiddleware({
    target: tradingServiceUrl,
    changeOrigin: true,
    pathRewrite: (path) =>
      path === "/" ? "/api/v1/orders" : `/api/v1/orders${path}`,
    on: {
      proxyReq: (proxyReq, req) => {
        const expressReq = req as Request;
        if (expressReq.body && Object.keys(expressReq.body).length > 0) {
          const bodyData = JSON.stringify(expressReq.body);

          proxyReq.setHeader("Content-Type", "application/json");
          proxyReq.setHeader(
            "Content-Length",
            Buffer.byteLength(bodyData),
          );

          proxyReq.write(bodyData);
        }
      },
    },
  }),
);

app.use(
  "/api/v1/trades",
  createProxyMiddleware({
    target: tradingServiceUrl,
    changeOrigin: true,
    pathRewrite: (path) =>
      path === "/" ? "/api/v1/trades" : `/api/v1/trades${path}`,
  }),
);

app.use(
  "/api/v1/instruments",
  createProxyMiddleware({
    target: tradingServiceUrl,
    changeOrigin: true,
    pathRewrite: (path) =>
      path === "/" ? "/api/v1/instruments" : `/api/v1/instruments${path}`,
  }),
);

app.use(
  "/api/v1/portfolio",
  createProxyMiddleware({
    target: tradingServiceUrl,
    changeOrigin: true,
    pathRewrite: (path) =>
      path === "/" ? "/api/v1/portfolio" : `/api/v1/portfolio${path}`,
  }),
);

app.use(
  "/api/v1/market-data",
  createProxyMiddleware({
    target: marketServiceUrl,
    changeOrigin: true,
    pathRewrite: (path) =>
      path === "/" ? "/api/v1/market-data" : `/api/v1/market-data${path}`,
  }),
);

app.get("/health", (_req: Request, res: Response) => {
  res.status(200).json({
    status: "UP",
    service: "api-gateway",
  });
});

app.get("/ready", (_req: Request, res: Response) => {
  res.status(200).json({
    status: "READY",
    service: "api-gateway",
  });
});

app.use((_req: Request, res: Response) => {
  res.status(404).json({
    error: "NOT_FOUND",
    message: "Route not found",
  });
});

app.use(
  (
    error: Error & { code?: string; response?: { status?: number } },
    req: Request,
    res: Response,
    _next: NextFunction,
  ) => {
    const requestId = res.locals.requestId;

    console.error({
      requestId,
      method: req.method,
      path: req.originalUrl,
      error: error.message,
      code: error.code,
    });

    if (
      error.code === "ECONNREFUSED" ||
      error.code === "ECONNRESET" ||
      error.code === "ETIMEDOUT"
    ) {
      res.status(503).json({
        error: "SERVICE_UNAVAILABLE",
        message: "Upstream service unavailable",
        requestId,
      });
      return;
    }

    res.status(500).json({
      error: "INTERNAL_SERVER_ERROR",
      message: "Internal server error",
      requestId,
    });
  },
);

export { app };
