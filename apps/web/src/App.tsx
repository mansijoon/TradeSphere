import { useEffect, useMemo, useState } from "react";
import {
  createOrder,
  getInstruments,
  getLatestMarketTick,
  getMarketHistory,
  getPortfolio,
  getOpenOrders,
  getOrderHistory,
  getTradeHistory,
  type Instrument,
  type MarketTick,
  type PortfolioResult,
  type Order,
  type Trade,
} from "./api";
import { createMarketWebSocket, type TradeExecutedEvent } from "./websocket";
import "./App.css";

type Side = "BUY" | "SELL";
type BottomTab = "POSITIONS" | "OPEN ORDERS" | "ORDER HISTORY" | "TRADES";



function App() {
  const [symbol, setSymbol] = useState("AAPL");
  const [side, setSide] = useState<Side>("BUY");
  const [quantity, setQuantity] = useState("1");
  const [price, setPrice] = useState("106.00");
  const [instruments, setInstruments] = useState<Instrument[]>([]);
  const [marketTick, setMarketTick] = useState<MarketTick | null>(null);
  const [marketHistory, setMarketHistory] = useState<MarketTick[]>([]);
  const [loadingMarket, setLoadingMarket] = useState(true);
  const [marketError, setMarketError] = useState<string | null>(null);
  const [wsConnected, setWsConnected] = useState(false);
  const [lastTrade, setLastTrade] = useState<TradeExecutedEvent | null>(null);
  const [portfolio, setPortfolio] = useState<PortfolioResult | null>(null);
  const [orderSubmitting, setOrderSubmitting] = useState(false);
  const [orderMessage, setOrderMessage] = useState<string | null>(null);
  const [openOrders, setOpenOrders] = useState<Order[]>([]);
  const [orderHistory, setOrderHistory] = useState<Order[]>([]);
  const [tradeHistory, setTradeHistory] = useState<Trade[]>([]);
  const [bottomTab, setBottomTab] = useState<BottomTab>("POSITIONS");

  useEffect(() => {
    let cancelled = false;

    async function loadInstruments() {
      try {
        const data = await getInstruments();
        if (!cancelled) {
          setInstruments(data);
          if (data.length > 0) {
            setSymbol(data[0].symbol);
          }
        }
      } catch (error) {
        if (!cancelled) {
          setMarketError(error instanceof Error ? error.message : "Failed to load instruments");
        }
      }
    }

    void loadInstruments();

    return () => {
      cancelled = true;
    };
  }, []);

  const selectedInstrument = useMemo(
    () => instruments.find((instrument) => instrument.symbol === symbol),
    [instruments, symbol],
  );

  useEffect(() => {
    if (!selectedInstrument) {
      return;
    }

    let cancelled = false;
    const instrumentId = selectedInstrument.id;

    async function loadMarketHistory() {
      try {
        const history = await getMarketHistory(instrumentId);
        if (!cancelled) {
          setMarketHistory(history);
        }
      } catch {
        if (!cancelled) {
          setMarketHistory([]);
        }
      }
    }

    void loadMarketHistory();

    return () => {
      cancelled = true;
    };
  }, [selectedInstrument]);

  useEffect(() => {
    if (!selectedInstrument) {
      return;
    }

    let cancelled = false;

    async function loadMarketTick() {
      try {
        const instrument = selectedInstrument;
        if (!instrument) {
          return;
        }

        const tick = await getLatestMarketTick(instrument.id);

        if (!cancelled) {
          setMarketTick(tick);
          setPrice(tick.price.toFixed(2));
          setMarketError(null);
        }
      } catch (error) {
        if (!cancelled) {
          setMarketError(error instanceof Error ? error.message : "Failed to load market data");
        }
      } finally {
        if (!cancelled) {
          setLoadingMarket(false);
        }
      }
    }

    void loadMarketTick();

    return () => {
      cancelled = true;
    };
  }, [selectedInstrument]);

  const symbols = instruments.length > 0
    ? instruments.map((instrument) => instrument.symbol)
    : ["AAPL", "MSFT", "NVDA", "TSLA"];

  const currentPrice = marketTick?.price ?? 106;

  useEffect(() => {
    if (!selectedInstrument || !marketTick?.price) {
      return;
    }

    let cancelled = false;
    const instrumentId = selectedInstrument.id;
    const marketPrice = marketTick.price;

    async function loadPortfolio() {
      try {
        const result = await getPortfolio(
          import.meta.env.VITE_ACCOUNT_ID ??
            "8331c25a-3202-4322-a5e1-1ea0a54d896e",
          instrumentId,
          marketPrice,
        );

        if (!cancelled) {
          setPortfolio(result);
        }
      } catch (error) {
        if (!cancelled) {
          setMarketError(
            error instanceof Error
              ? error.message
              : "Failed to load portfolio",
          );
        }
      }
    }

    void loadPortfolio();

    return () => {
      cancelled = true;
    };
  }, [selectedInstrument, marketTick?.price]);

  const chartTicks = useMemo(
    () =>
      [...marketHistory]
        .sort(
          (a, b) =>
            new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime(),
        )
        .slice(-24),
    [marketHistory],
  );

  const chartPoints = useMemo(() => {
    if (chartTicks.length === 0) {
      return "";
    }

    const prices = chartTicks.map((tick) => tick.price);
    const min = Math.min(...prices);
    const max = Math.max(...prices);
    const range = max - min || 1;

    return chartTicks
      .map((tick, index) => {
        const x =
          chartTicks.length === 1
            ? 450
            : (index / (chartTicks.length - 1)) * 900;
        const y = 300 - ((tick.price - min) / range) * 240;
        return `${x.toFixed(1)},${y.toFixed(1)}`;
      })
      .join(" ");
  }, [chartTicks]);

  async function handlePlaceOrder() {
    if (!selectedInstrument) {
      setOrderMessage("Select an instrument first.");
      return;
    }

    const parsedQuantity = Number(quantity);
    const parsedPrice = Number(price);

    if (!Number.isFinite(parsedQuantity) || parsedQuantity <= 0) {
      setOrderMessage("Quantity must be greater than zero.");
      return;
    }

    if (!Number.isFinite(parsedPrice) || parsedPrice <= 0) {
      setOrderMessage("Limit price must be greater than zero.");
      return;
    }

    setOrderSubmitting(true);
    setOrderMessage(null);

    try {
      const order = await createOrder({
        accountId:
          import.meta.env.VITE_ACCOUNT_ID ??
          "8331c25a-3202-4322-a5e1-1ea0a54d896e",
        instrumentId: selectedInstrument.id,
        clientOrderId: `web-${Date.now()}`,
        side,
        orderType: "LIMIT",
        timeInForce: "GTC",
        price: parsedPrice,
        quantity: parsedQuantity,
      });

      setOrderMessage(
        `${order.status} · ${order.id.slice(0, 8)} · ${order.side} ${order.quantity} ${selectedInstrument.symbol}`,
      );
    } catch (error) {
      setOrderMessage(
        error instanceof Error ? error.message : "Order submission failed.",
      );
    } finally {
      setOrderSubmitting(false);
    }
  }

  useEffect(() => {
    const accountId =
      import.meta.env.VITE_ACCOUNT_ID ??
      "8331c25a-3202-4322-a5e1-1ea0a54d896e";

    let cancelled = false;

    async function loadTradingHistory() {
      try {
        const [open, history, trades] = await Promise.all([
          getOpenOrders(accountId),
          getOrderHistory(accountId),
          getTradeHistory(accountId),
        ]);

        if (!cancelled) {
          setOpenOrders(open);
          setOrderHistory(history);
          setTradeHistory(trades);
        }
      } catch {
        if (!cancelled) {
          setOpenOrders([]);
          setOrderHistory([]);
          setTradeHistory([]);
        }
      }
    }

    void loadTradingHistory();

    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    if (!selectedInstrument) {
      return;
    }

    const cleanup = createMarketWebSocket(
      `market:${selectedInstrument.id}`,
      (event) => {
        if (!("eventType" in event) || event.eventType !== "TradeExecuted") {
          return;
        }

        const trade = event;
        setLastTrade(trade);

        if (trade.instrumentId === selectedInstrument.id) {
          setMarketTick((previous) => ({
            id: previous?.id ?? 0,
            instrumentId: trade.instrumentId,
            price: trade.price,
            quantity: trade.quantity,
            bidPrice: previous?.bidPrice ?? null,
            askPrice: previous?.askPrice ?? null,
            timestamp: trade.timestamp,
          }));

          setPrice(trade.price.toFixed(2));

          const accountId =
            import.meta.env.VITE_ACCOUNT_ID ??
            "8331c25a-3202-4322-a5e1-1ea0a54d896e";

          void Promise.all([
            getOpenOrders(accountId),
            getOrderHistory(accountId),
            getTradeHistory(accountId),
            getPortfolio(accountId, selectedInstrument.id, trade.price),
          ]).then(([open, history, trades, portfolioResult]) => {
            setOpenOrders(open);
            setOrderHistory(history);
            setTradeHistory(trades);
            setPortfolio(portfolioResult);
          }).catch(() => {
            // Keep the last known terminal state if a refresh fails.
          });
        }
      },
      setWsConnected,
    );

    return cleanup;
  }, [selectedInstrument]);

  return (
    <div className="terminal">
      <header className="topbar">
        <div className="brand">
          <div className="brand-mark">M</div>
          <div>
            <div className="brand-name">MARKETFORGE</div>
            <div className="brand-subtitle">TRADING TERMINAL</div>
          </div>
        </div>

        <div className="connection">
          <span className={`status-dot ${wsConnected ? "connected" : "disconnected"}`} />
          <span>{wsConnected ? "MARKET LIVE" : "MARKET OFFLINE"}</span>
          <span className="separator">|</span>
          <span>WS {wsConnected ? "CONNECTED" : "DISCONNECTED"}</span>
        </div>

        <div className="account">
          <span className="account-label">PAPER ACCOUNT</span>
          <strong>
            ${portfolio?.availableBalance.toLocaleString("en-US", {
              minimumFractionDigits: 2,
              maximumFractionDigits: 2,
            }) ?? "100,000.00"}
          </strong>
        </div>
      </header>

      <nav className="symbolbar">
        <div className="symbol-tabs">
          {symbols.map((item) => (
            <button
              key={item}
              className={`symbol-tab ${symbol === item ? "active" : ""}`}
              onClick={() => setSymbol(item)}
            >
              {item}
            </button>
          ))}
        </div>

        <div className="session-info">
          <span>SIMULATION</span>
          <span>UTC 02:58:12</span>
        </div>
      </nav>

      <main className="workspace">
        <section className="market-header panel">
          <div>
            <div className="instrument-name">{symbol}</div>
            <div className="instrument-meta">
              {selectedInstrument
                ? `${selectedInstrument.name} · ${selectedInstrument.assetType} · ${selectedInstrument.status}`
                : "Loading instrument..."}
            </div>
          </div>

          <div className="quote">
            <span className="quote-price">{loadingMarket ? "—" : currentPrice.toFixed(2)}</span>
            <span className="quote-change positive">+1.24 (+1.18%)</span>
          </div>

          {marketError && <div className="market-error">{marketError}</div>}

          <div className="stats">
            <div>
              <span>OPEN</span>
              <strong>104.72</strong>
            </div>
            <div>
              <span>HIGH</span>
              <strong>106.48</strong>
            </div>
            <div>
              <span>LOW</span>
              <strong>103.91</strong>
            </div>
            <div>
              <span>VOLUME</span>
              <strong>48.2K</strong>
            </div>
          </div>

          {lastTrade && lastTrade.instrumentId === selectedInstrument?.id && (
            <div className="latest-trade">
              <span>LAST TRADE</span>
              <strong>{lastTrade.price.toFixed(2)}</strong>
              <span>× {lastTrade.quantity}</span>
            </div>
          )}
        </section>

        <section className="main-grid">
          <div className="chart-panel panel">
            <div className="panel-header">
              <div>
                <span className="panel-title">PRICE</span>
                <span className="panel-muted">AAPL / USD</span>
              </div>

              <div className="timeframes">
                {["1m", "5m", "15m", "1h", "1D"].map((frame, index) => (
                  <button
                    key={frame}
                    className={index === 1 ? "selected" : ""}
                  >
                    {frame}
                  </button>
                ))}
              </div>
            </div>

            <div className="chart">
              <div className="chart-grid" />
              <svg
                className="price-line"
                viewBox="0 0 900 360"
                preserveAspectRatio="none"
                aria-label="Live market price chart"
              >
                {chartPoints && (
                  <>
                    <polyline
                      points={chartPoints}
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2.5"
                    />
                    <polyline
                      points={`0,350 ${chartPoints} 900,350`}
                      fill="currentColor"
                      opacity="0.07"
                    />
                  </>
                )}
              </svg>

              <div className="price-marker">{currentPrice.toFixed(2)}</div>

              <div className="chart-axis">
                <span>01:00</span>
                <span>01:30</span>
                <span>02:00</span>
                <span>02:30</span>
                <span>03:00</span>
              </div>
            </div>
          </div>

          <aside className="orderbook panel">
            <div className="panel-header">
              <span className="panel-title">ORDER BOOK</span>
              <span className="panel-muted">PRICE / SIZE</span>
            </div>

            <div className="book-head">
              <span>PRICE</span>
              <span>SIZE</span>
            </div>

            <div className="book-rows asks">
              <div className="book-row">
                <span>{marketTick?.askPrice?.toFixed(2) ?? "—"}</span>
                <span>{marketTick?.askPrice ? "LIVE" : "—"}</span>
              </div>
            </div>

            <div className="spread">
              <span>SPREAD</span>
              <strong>
                {marketTick?.bidPrice != null && marketTick.askPrice != null
                  ? (marketTick.askPrice - marketTick.bidPrice).toFixed(2)
                  : "—"}
              </strong>
            </div>

            <div className="book-rows bids">
              <div className="book-row">
                <span>{marketTick?.bidPrice?.toFixed(2) ?? "—"}</span>
                <span>{marketTick?.bidPrice ? "LIVE" : "—"}</span>
              </div>
            </div>
          </aside>

          <aside className="order-entry panel">
            <div className="panel-header">
              <span className="panel-title">ORDER ENTRY</span>
              <span className="panel-muted">LIMIT</span>
            </div>

            <div className="side-toggle">
              <button
                className={side === "BUY" ? "buy active" : "buy"}
                onClick={() => setSide("BUY")}
              >
                BUY
              </button>
              <button
                className={side === "SELL" ? "sell active" : "sell"}
                onClick={() => setSide("SELL")}
              >
                SELL
              </button>
            </div>

            <label>
              <span>SYMBOL</span>
              <select value={symbol} onChange={(e) => setSymbol(e.target.value)}>
                {symbols.map((item) => (
                  <option key={item}>{item}</option>
                ))}
              </select>
            </label>

            <label>
              <span>QUANTITY</span>
              <input
                value={quantity}
                onChange={(e) => setQuantity(e.target.value)}
                inputMode="decimal"
              />
            </label>

            <label>
              <span>LIMIT PRICE</span>
              <input
                value={price}
                onChange={(e) => setPrice(e.target.value)}
                inputMode="decimal"
              />
            </label>

            <div className="order-summary">
              <span>EST. VALUE</span>
              <strong>
                ${(Number(quantity || 0) * Number(price || 0)).toFixed(2)}
              </strong>
            </div>

            <button
              className={`submit-order ${side.toLowerCase()}`}
              onClick={() => void handlePlaceOrder()}
              disabled={orderSubmitting}
            >
              {orderSubmitting ? "SUBMITTING..." : `PLACE ${side} ORDER`}
            </button>

            {orderMessage && (
              <div className="order-message" role="status">
                {orderMessage}
              </div>
            )}

            <div className="risk-note">
              <span>AVAILABLE</span>
              <strong>
              ${portfolio?.availableBalance.toLocaleString("en-US", {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2,
              }) ?? "100,000.00"}
            </strong>
            </div>
          </aside>
        </section>

        <section className="bottom-panel panel">
          <div className="bottom-tabs">
            {(["POSITIONS", "OPEN ORDERS", "ORDER HISTORY", "TRADES"] as BottomTab[]).map(
              (tab) => (
                <button
                  key={tab}
                  className={bottomTab === tab ? "active" : ""}
                  onClick={() => setBottomTab(tab)}
                >
                  {tab}
                </button>
              ),
            )}
          </div>

          <div className="table-wrap">
            {bottomTab === "POSITIONS" && (
              <table>
                <thead>
                  <tr>
                    <th>SYMBOL</th>
                    <th>QTY</th>
                    <th>AVG ENTRY</th>
                    <th>MARK</th>
                    <th>UNREALIZED P&L</th>
                    <th>REALIZED P&L</th>
                    <th>EXPOSURE</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td className="symbol-cell">{symbol}</td>
                    <td>{portfolio ? portfolio.quantity : "—"}</td>
                    <td>{portfolio ? `$${portfolio.averageEntryPrice.toFixed(2)}` : "—"}</td>
                    <td>${currentPrice.toFixed(2)}</td>
                    <td className={portfolio && portfolio.unrealizedPnl >= 0 ? "positive" : "negative"}>
                      {portfolio
                        ? `${portfolio.unrealizedPnl >= 0 ? "+" : ""}$${portfolio.unrealizedPnl.toFixed(2)}`
                        : "—"}
                    </td>
                    <td>
                      {portfolio
                        ? `${portfolio.realizedPnl >= 0 ? "+" : ""}$${portfolio.realizedPnl.toFixed(2)}`
                        : "—"}
                    </td>
                    <td>{portfolio ? `$${portfolio.marketValue.toFixed(2)}` : "—"}</td>
                  </tr>
                </tbody>
              </table>
            )}

            {bottomTab === "OPEN ORDERS" && (
              <table>
                <thead>
                  <tr>
                    <th>SYMBOL</th>
                    <th>SIDE</th>
                    <th>TYPE</th>
                    <th>PRICE</th>
                    <th>QTY</th>
                    <th>FILLED</th>
                    <th>REMAINING</th>
                    <th>STATUS</th>
                    <th>TIME</th>
                  </tr>
                </thead>
                <tbody>
                  {openOrders.map((order) => {
                    const instrument = instruments.find(
                      (item) => item.id === order.instrumentId,
                    );

                    return (
                      <tr key={order.id}>
                        <td className="symbol-cell">
                          {instrument?.symbol ?? order.instrumentId.slice(0, 8)}
                        </td>
                        <td className={order.side === "BUY" ? "positive" : "negative"}>
                          {order.side}
                        </td>
                        <td>{order.orderType}</td>
                        <td>${order.price.toFixed(2)}</td>
                        <td>{order.quantity}</td>
                        <td>{order.filledQuantity}</td>
                        <td>{order.remainingQuantity}</td>
                        <td>{order.status}</td>
                        <td>{new Date(order.createdAt).toLocaleTimeString()}</td>
                      </tr>
                    );
                  })}
                  {openOrders.length === 0 && (
                    <tr><td colSpan={9}>NO OPEN ORDERS</td></tr>
                  )}
                </tbody>
              </table>
            )}

            {bottomTab === "ORDER HISTORY" && (
              <table>
                <thead>
                  <tr>
                    <th>SYMBOL</th>
                    <th>SIDE</th>
                    <th>TYPE</th>
                    <th>PRICE</th>
                    <th>QTY</th>
                    <th>FILLED</th>
                    <th>STATUS</th>
                    <th>CLIENT ORDER ID</th>
                    <th>TIME</th>
                  </tr>
                </thead>
                <tbody>
                  {orderHistory.map((order) => {
                    const instrument = instruments.find(
                      (item) => item.id === order.instrumentId,
                    );

                    return (
                      <tr key={order.id}>
                        <td className="symbol-cell">
                          {instrument?.symbol ?? order.instrumentId.slice(0, 8)}
                        </td>
                        <td className={order.side === "BUY" ? "positive" : "negative"}>
                          {order.side}
                        </td>
                        <td>{order.orderType}</td>
                        <td>${order.price.toFixed(2)}</td>
                        <td>{order.quantity}</td>
                        <td>{order.filledQuantity}</td>
                        <td>{order.status}</td>
                        <td>{order.clientOrderId}</td>
                        <td>{new Date(order.createdAt).toLocaleTimeString()}</td>
                      </tr>
                    );
                  })}
                  {orderHistory.length === 0 && (
                    <tr><td colSpan={9}>NO ORDER HISTORY</td></tr>
                  )}
                </tbody>
              </table>
            )}

            {bottomTab === "TRADES" && (
              <table>
                <thead>
                  <tr>
                    <th>SYMBOL</th>
                    <th>PRICE</th>
                    <th>QTY</th>
                    <th>VALUE</th>
                    <th>EXECUTED</th>
                    <th>TRADE ID</th>
                  </tr>
                </thead>
                <tbody>
                  {tradeHistory.map((trade) => {
                    const instrument = instruments.find(
                      (item) => item.id === trade.instrumentId,
                    );

                    return (
                      <tr key={trade.id}>
                        <td className="symbol-cell">
                          {instrument?.symbol ?? trade.instrumentId.slice(0, 8)}
                        </td>
                        <td>${trade.price.toFixed(2)}</td>
                        <td>{trade.quantity}</td>
                        <td>${(trade.price * trade.quantity).toFixed(2)}</td>
                        <td>{new Date(trade.executedAt).toLocaleTimeString()}</td>
                        <td>{trade.externalTradeId}</td>
                      </tr>
                    );
                  })}
                  {tradeHistory.length === 0 && (
                    <tr><td colSpan={6}>NO TRADES</td></tr>
                  )}
                </tbody>
              </table>
            )}
          </div>
        </section>

      </main>

      <footer className="footer">
        <span>MARKETFORGE ENGINE</span>
        <span>•</span>
        <span>C++ MATCHING ENGINE</span>
        <span>•</span>
        <span>KAFKA EVENT BUS</span>
        <span>•</span>
        <span>JAVA TRADING SERVICES</span>
      </footer>
    </div>
  );
}

export default App;
