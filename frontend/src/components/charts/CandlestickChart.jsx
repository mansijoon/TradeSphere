import { useEffect, useRef } from "react";
import { createChart } from "lightweight-charts";

export default function CandlestickChart({ symbol = "AAPL" }) {
  const chartRef = useRef();

  useEffect(() => {
    const chart = createChart(chartRef.current, {
      layout: { background: { color: "#151b26" }, textColor: "#fff" },
      height: 300,
    });

    const candleSeries = chart.addCandlestickSeries();

    // fake data for now (replace later with API)
    const data = [
      { time: "2026-06-20", open: 100, high: 110, low: 95, close: 105 },
      { time: "2026-06-21", open: 105, high: 115, low: 101, close: 112 },
      { time: "2026-06-22", open: 112, high: 120, low: 108, close: 119 },
    ];

    candleSeries.setData(data);

    return () => chart.remove();
  }, [symbol]);

  return <div ref={chartRef} />;
}
