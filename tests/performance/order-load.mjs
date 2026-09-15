import { performance } from "node:perf_hooks";
import assert from "node:assert/strict";

const BASE_URL = process.env.BASE_URL ?? "http://localhost:3000";
const ACCOUNT_ID =
  process.env.ACCOUNT_ID ??
  "8331c25a-3202-4322-a5e1-1ea0a54d896e";
const INSTRUMENT_ID =
  process.env.INSTRUMENT_ID ??
  "00000000-0000-0000-0000-000000000001";

const TOTAL_REQUESTS = Number(process.env.REQUESTS ?? 100);
const CONCURRENCY = Number(process.env.CONCURRENCY ?? 10);

const latencies = [];
let successful = 0;
let failed = 0;

async function submitOrder(index) {
  const clientOrderId = `PERF-${Date.now()}-${index}-${Math.random()
    .toString(36)
    .slice(2, 8)}`;

  const start = performance.now();

  try {
    const response = await fetch(`${BASE_URL}/api/v1/orders`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: "Bearer test-token",
      },
      body: JSON.stringify({
        accountId: ACCOUNT_ID,
        instrumentId: INSTRUMENT_ID,
        clientOrderId,
        side: index % 2 === 0 ? "BUY" : "SELL",
        orderType: "LIMIT",
        timeInForce: "IOC",
        price: 1,
        quantity: 1,
      }),
    });

    const elapsed = performance.now() - start;
    latencies.push(elapsed);

    if (response.ok) {
      successful++;
    } else {
      failed++;
      await response.text();
    }
  } catch {
    latencies.push(performance.now() - start);
    failed++;
  }
}

async function run() {
  console.log("=== MarketForge Performance Test ===");
  console.log(`Gateway:       ${BASE_URL}`);
  console.log(`Requests:      ${TOTAL_REQUESTS}`);
  console.log(`Concurrency:   ${CONCURRENCY}`);
  console.log("");

  const testStart = performance.now();
  let next = 0;

  async function worker() {
    while (true) {
      const index = next++;

      if (index >= TOTAL_REQUESTS) {
        return;
      }

      await submitOrder(index);
    }
  }

  await Promise.all(
    Array.from(
      { length: Math.min(CONCURRENCY, TOTAL_REQUESTS) },
      () => worker()
    )
  );

  const duration = (performance.now() - testStart) / 1000;

  latencies.sort((a, b) => a - b);

  const percentile = (p) => {
    const index = Math.min(
      latencies.length - 1,
      Math.ceil((p / 100) * latencies.length) - 1
    );
    return latencies[index];
  };

  const avg =
    latencies.reduce((sum, value) => sum + value, 0) /
    latencies.length;

  console.log("Results");
  console.log("-------");
  console.log(`Duration:      ${duration.toFixed(2)} s`);
  console.log(`Throughput:    ${(TOTAL_REQUESTS / duration).toFixed(2)} req/s`);
  console.log(`Successful:    ${successful}`);
  console.log(`Failed:        ${failed}`);
  console.log(`Success rate:  ${((successful / TOTAL_REQUESTS) * 100).toFixed(2)}%`);
  console.log("");
  console.log("Latency");
  console.log("-------");
  console.log(`Average:       ${avg.toFixed(2)} ms`);
  console.log(`p50:           ${percentile(50).toFixed(2)} ms`);
  console.log(`p95:           ${percentile(95).toFixed(2)} ms`);
  console.log(`p99:           ${percentile(99).toFixed(2)} ms`);
  console.log(`Max:           ${Math.max(...latencies).toFixed(2)} ms`);

  assert.equal(
    latencies.length,
    TOTAL_REQUESTS,
    "Not all requests produced a measurement"
  );

  console.log("");
  console.log("PASS: performance benchmark completed.");
}

run().catch((error) => {
  console.error(error);
  process.exit(1);
});
