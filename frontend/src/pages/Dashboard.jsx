import { useEffect, useState } from "react";
import axios from "axios";

import PriceGrid from "../components/PriceGrid";
import HoldingsTable from "../components/HoldingsTable";
import PortfolioKPI from "../components/PortfolioKPI";

import Watchlist from "../components/panels/Watchlist";
import OrderPanel from "../components/panels/OrderPanel";
import ChartPanel from "../components/charts/ChartPanel";

export default function Dashboard() {
  const [portfolio, setPortfolio] = useState({
    holdings: [],
    cashBalance: 0,
    investedValue: 0,
    marketValue: 0
  });

  const [prices, setPrices] = useState({});
  const [chartData, setChartData] = useState([]);
  const [selectedSymbol, setSelectedSymbol] = useState("AAPL");

  const token = localStorage.getItem("token");

  useEffect(() => {
    const fetchPortfolio = async () => {
      const res = await axios.get(
        "http://localhost:5000/api/portfolio-summary/me",
        {
          headers: {
            Authorization: `Bearer ${token}`
          }
        }
      );

      setPortfolio(res.data);
    };

    fetchPortfolio();

    const payload = JSON.parse(
      atob(token.split(".")[1])
    );

    const userId = payload.userId;

    const socket = new WebSocket("ws://localhost:5000");

    socket.onopen = () => {
      socket.send(
        JSON.stringify({
          type: "subscribe_portfolio",
          userId
        })
      );
    };

    socket.onmessage = (event) => {
      const data = JSON.parse(event.data);
      console.log("WS_MESSAGE", data);


      if (data.type === "price_update") {
        setChartData(prev => {
          if (data.symbol !== selectedSymbol) return prev;

          const next = [
            ...prev,
            {
              time: new Date().toLocaleTimeString(),
              price: data.price
            }
          ];

          return next.slice(-50);
        });

        setPrices((prev) => ({
          ...prev,
          [data.symbol]: data.price
        }));
      }

      if (
        (data.type === "portfolio_update" ||
          data.type === "portfolio_snapshot") &&
        data.data
      ) {
console.log("WS_PORTFOLIO", data.data);
        setPortfolio(data.data);
      }
    };

    return () => socket.close();
  }, []);

  return (
    <div style={{ padding: 20, background: "#0b0f14", color: "white" }}>
      <h1>TradeSphere Terminal</h1>

      <PortfolioKPI portfolio={portfolio} />

      <div
        style={{
          display: "grid",
          gridTemplateColumns: "250px 1fr 300px",
          gap: 12
        }}
      >
        <Watchlist
          prices={prices}
          onSelect={setSelectedSymbol}
        />

        <div>
          <PriceGrid
            prices={prices}
            onSelect={setSelectedSymbol}
          />

          <ChartPanel
            data={chartData}
            symbol={selectedSymbol}
          />

          <HoldingsTable
            holdings={portfolio?.holdings || []}
          />
        </div>

        <OrderPanel
          prices={prices}
          selectedSymbol={selectedSymbol}
        />
      </div>
    </div>
  );
}
