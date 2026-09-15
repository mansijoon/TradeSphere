import assert from "node:assert/strict";
import { execFileSync } from "node:child_process";

const GATEWAY = process.env.GATEWAY_URL ?? "http://localhost:3000";
const ACCOUNT_ID =
  process.env.E2E_ACCOUNT_ID ??
  "8331c25a-3202-4322-a5e1-1ea0a54d896e";
const INSTRUMENT_ID =
  process.env.E2E_INSTRUMENT_ID ??
  "00000000-0000-0000-0000-000000000001";

const suffix = Date.now();
const sellClientOrderId = `E2E-AUTO-SELL-${suffix}`;
const buyClientOrderId = `E2E-AUTO-BUY-${suffix}`;

function kubectl(args) {
  return execFileSync("kubectl", args, {
    encoding: "utf8",
    stdio: ["ignore", "pipe", "pipe"],
  }).trim();
}

async function postOrder(order) {
  const response = await fetch(`${GATEWAY}/api/v1/orders`, {
    method: "POST",
    headers: {
      Authorization: "Bearer test-token",
      "Content-Type": "application/json",
      Accept: "application/json",
    },
    body: JSON.stringify(order),
  });

  const body = await response.json();

  assert.equal(
    response.status,
    201,
    `Order submission failed: ${JSON.stringify(body)}`,
  );

  return body;
}

function queryPostgres(sql) {
  return kubectl([
    "exec",
    "-n",
    "marketforge-data",
    "deploy/postgres",
    "--",
    "psql",
    "-U",
    "marketforge",
    "-d",
    "marketforge",
    "-At",
    "-F",
    "|",
    "-c",
    sql,
  ]);
}

async function waitFor(condition, timeoutMs = 15000, intervalMs = 500) {
  const deadline = Date.now() + timeoutMs;

  while (Date.now() < deadline) {
    const result = await condition();

    if (result) {
      return result;
    }

    await new Promise((resolve) => setTimeout(resolve, intervalMs));
  }

  throw new Error("Timed out waiting for E2E condition");
}

console.log("=== MarketForge E2E Order Flow ===");
console.log(`Gateway:   ${GATEWAY}`);
console.log(`Account:   ${ACCOUNT_ID}`);
console.log(`Instrument: ${INSTRUMENT_ID}`);
console.log(`Run ID:    ${suffix}`);

const buy = await postOrder({
  accountId: ACCOUNT_ID,
  instrumentId: INSTRUMENT_ID,
  clientOrderId: buyClientOrderId,
  side: "BUY",
  orderType: "LIMIT",
  timeInForce: "GTC",
  price: 1,
  quantity: 1,
});

console.log(`BUY accepted:  ${buy.id}`);

const sell = await postOrder({
  accountId: ACCOUNT_ID,
  instrumentId: INSTRUMENT_ID,
  clientOrderId: sellClientOrderId,
  side: "SELL",
  orderType: "LIMIT",
  timeInForce: "IOC",
  price: 1,
  quantity: 1,
});

console.log(`SELL accepted: ${sell.id}`);

const sellId = sell.id;

await waitFor(() => {
  const result = queryPostgres(`
    SELECT status
    FROM orders
    WHERE id = '${sellId}'
  `);

  return result.trim() === "FILLED";
});

const orders = queryPostgres(`
  SELECT
    client_order_id,
    status,
    filled_quantity,
    remaining_quantity
  FROM orders
  WHERE id IN ('${buy.id}', '${sellId}')
  ORDER BY client_order_id
`);

console.log("\nOrders:");
console.log(orders);

const trade = await waitFor(() => {
  const result = queryPostgres(`
    SELECT
      id,
      buy_order_id,
      sell_order_id,
      price,
      quantity
    FROM trades
    WHERE buy_order_id IN ('${buy.id}', '${sellId}')
       OR sell_order_id IN ('${buy.id}', '${sellId}')
    ORDER BY executed_at DESC
    LIMIT 1
  `);

  return result || null;
});

console.log("\nTrade involving submitted order:");
console.log(trade);

assert.ok(trade, "No persisted trade references the submitted order");

const redisKeys = kubectl([
  "exec",
  "-n",
  "marketforge",
  "deploy/redis",
  "--",
  "redis-cli",
  "KEYS",
  `marketforge:order:${ACCOUNT_ID}:${sellClientOrderId}`
]);

assert.ok(
  redisKeys.includes(`marketforge:order:${ACCOUNT_ID}:${sellClientOrderId}`),
  "Submitted SELL order was not cached in Redis"
);

console.log("\nPASS: complete order flow verified.");

console.log("\nPASS: complete order flow verified.");
