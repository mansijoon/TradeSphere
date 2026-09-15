import assert from "node:assert/strict";

process.env.NODE_ENV = "test";
import { describe, it } from "node:test";
import request from "supertest";

import { app } from "../app.js";

describe("API Gateway", () => {
  it("returns health status", async () => {
    const response = await request(app).get("/health");

    assert.equal(response.status, 200);
    assert.deepEqual(response.body, {
      status: "UP",
      service: "api-gateway",
    });
  });

  it("returns readiness status", async () => {
    const response = await request(app).get("/ready");

    assert.equal(response.status, 200);
    assert.deepEqual(response.body, {
      status: "READY",
      service: "api-gateway",
    });
  });

  it("returns 404 for unknown routes", async () => {
    const response = await request(app).get("/does-not-exist");

    assert.equal(response.status, 404);
    assert.equal(response.body.error, "NOT_FOUND");
  });

  it("returns a generated request ID", async () => {
    const response = await request(app).get("/health");

    assert.equal(response.status, 200);
    assert.ok(response.headers["x-request-id"]);
  });

  it("preserves a client-provided request ID", async () => {
    const response = await request(app)
      .get("/health")
      .set("x-request-id", "test-request-123");

    assert.equal(response.status, 200);
    assert.equal(response.headers["x-request-id"], "test-request-123");
  });

  it("sets security headers", async () => {
    const response = await request(app).get("/health");

    assert.ok(response.headers["x-content-type-options"]);
    assert.ok(response.headers["x-frame-options"]);
  });

  it("rejects an unauthenticated order request", async () => {
    const response = await request(app)
      .post("/api/v1/orders")
      .send({
        accountId: "8331c25a-3202-4322-a5e1-1ea0a54d896e",
        instrumentId: "00000000-0000-0000-0000-000000000001",
        clientOrderId: "auth-test",
        side: "BUY",
        orderType: "LIMIT",
        timeInForce: "DAY",
        price: 106,
        quantity: 1,
      });

    assert.equal(response.status, 401);
    assert.equal(response.body.error, "UNAUTHORIZED");
    assert.ok(response.body.requestId);
  });

  it("rejects an empty bearer token", async () => {
    const response = await request(app)
      .post("/api/v1/orders")
      .set("Authorization", "Bearer ")
      .send({
        accountId: "8331c25a-3200-4322-a5e1-1ea0a54d896e",
        instrumentId: "00000000-0000-0000-0000-000000000001",
        clientOrderId: "auth-test-empty",
        side: "BUY",
        orderType: "LIMIT",
        timeInForce: "DAY",
        price: 106,
        quantity: 1,
      });

    assert.equal(response.status, 401);
    assert.equal(response.body.error, "UNAUTHORIZED");
  });

  it("rejects an invalid order before proxying", async () => {
    const response = await request(app)
      .post("/api/v1/orders")
      .set("Authorization", "Bearer test-token")
      .send({
        accountId: "not-a-uuid",
        instrumentId: "not-a-uuid",
        clientOrderId: "",
        side: "INVALID",
        orderType: "LIMIT",
        timeInForce: "DAY",
        quantity: -1,
      });

    assert.equal(response.status, 400);
    assert.equal(response.body.error, "VALIDATION_ERROR");
    assert.ok(Array.isArray(response.body.details));
  });

  it("rejects a LIMIT order without price", async () => {
    const response = await request(app)
      .post("/api/v1/orders")
      .set("Authorization", "Bearer test-token")
      .send({
        accountId: "8331c25a-3202-4322-a5e1-1ea0a54d896e",
        instrumentId: "00000000-0000-0000-0000-000000000001",
        clientOrderId: "gateway-validation-test",
        side: "BUY",
        orderType: "LIMIT",
        timeInForce: "DAY",
        quantity: 1,
      });

    assert.equal(response.status, 400);
    assert.equal(response.body.error, "VALIDATION_ERROR");
    assert.ok(
      response.body.details.some(
        (detail: { path: string[] }) => detail.path.includes("price"),
      ),
    );
  });

  it("sets the configured CORS origin", async () => {
    const response = await request(app)
      .get("/health")
      .set("Origin", "http://localhost:5173");

    assert.equal(
      response.headers["access-control-allow-origin"],
      "http://localhost:5173",
    );
  });
});
