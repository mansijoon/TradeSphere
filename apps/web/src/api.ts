const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:3000";

export type Instrument = {
  id: string;
  symbol: string;
  name: string;
  assetType: string;
  status: string;
};

export type MarketTick = {
  id: number;
  instrumentId: string;
  price: number;
  quantity: number;
  bidPrice: number | null;
  askPrice: number | null;
  timestamp: string;
};

async function request<T>(path: string): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      Authorization: "Bearer test-token",
      Accept: "application/json",
    },
  });

  if (!response.ok) {
    throw new Error(`API request failed: ${response.status}`);
  }

  return response.json() as Promise<T>;
}

export function getInstruments(): Promise<Instrument[]> {
  return request<Instrument[]>("/api/v1/instruments");
}

export function getLatestMarketTick(
  instrumentId: string,
): Promise<MarketTick> {
  return request<MarketTick>(
    `/api/v1/market-data/${instrumentId}/latest`,
  );
}


export function getMarketHistory(instrumentId: string): Promise<MarketTick[]> {
  return request<MarketTick[]>(
    `/api/v1/market-data/${instrumentId}/history`,
  );
}

export type CreateOrderRequest = {
  accountId: string;
  instrumentId: string;
  clientOrderId: string;
  side: "BUY" | "SELL";
  orderType: "LIMIT" | "MARKET";
  timeInForce: "DAY" | "GTC" | "IOC" | "FOK";
  price?: number;
  quantity: number;
};

export type CreatedOrder = CreateOrderRequest & {
  id: string;
  createdAt: string;
  updatedAt: string;
  filledQuantity: number;
  remainingQuantity: number;
  status: string;
};

export function createOrder(
  order: CreateOrderRequest,
): Promise<CreatedOrder> {
  return fetch(`${API_BASE_URL}/api/v1/orders`, {
    method: "POST",
    headers: {
      Authorization: "Bearer test-token",
      "Content-Type": "application/json",
      Accept: "application/json",
    },
    body: JSON.stringify(order),
  }).then(async (response) => {
    if (!response.ok) {
      const message = await response.text();
      throw new Error(message || `Order request failed: ${response.status}`);
    }

    return response.json() as Promise<CreatedOrder>;
  });
}


export type PortfolioResult = {
  accountId: string;
  instrumentId: string;
  quantity: number;
  averageEntryPrice: number;
  cash: number;
  availableBalance: number;
  marketValue: number;
  unrealizedPnl: number;
  realizedPnl: number;
  totalEquity: number;
};

export type Order = {
  accountId: string;
  instrumentId: string;
  clientOrderId: string;
  side: "BUY" | "SELL";
  orderType: "LIMIT" | "MARKET";
  timeInForce: "DAY" | "GTC" | "IOC" | "FOK";
  price: number;
  quantity: number;
  createdAt: string;
  filledQuantity: number;
  id: string;
  remainingQuantity: number;
  status: string;
  updatedAt: string;
};

export type Trade = {
  instrumentId: string;
  buyOrderId: string;
  sellOrderId: string;
  price: number;
  quantity: number;
  executedAt: string;
  externalTradeId: string;
  id: string;
};

export function getOpenOrders(accountId: string): Promise<Order[]> {
  return request<Order[]>(`/api/v1/orders/open/${accountId}`);
}

export function getOrderHistory(accountId: string): Promise<Order[]> {
  return request<Order[]>(`/api/v1/orders/history/${accountId}`);
}

export function getTradeHistory(accountId: string): Promise<Trade[]> {
  return request<Trade[]>(`/api/v1/trades/history/${accountId}`);
}

export function getPortfolio(
  accountId: string,
  instrumentId: string,
  currentMarketPrice: number,
): Promise<PortfolioResult> {
  return request<PortfolioResult>(
    `/api/v1/portfolio/${accountId}/${instrumentId}?currentMarketPrice=${encodeURIComponent(currentMarketPrice)}`,
  );
}
