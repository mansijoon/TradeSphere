import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
} from "recharts";

export default function ChartPanel({ data, symbol }) {
  if (!data || data.length === 0) {
    return (
      <div style={{ padding: 20, color: "#aaa" }}>
        Waiting for price data...
      </div>
    );
  }

  return (
    <div style={{ width: "100%", height: 260 }}>
      <h3 style={{ color: "#aaa" }}>{symbol} Chart</h3>

      <ResponsiveContainer width="100%" height="100%">
        <LineChart data={data}>
          <XAxis dataKey="time" hide />
          <YAxis domain={["auto", "auto"]} />
          <Tooltip />
          <Line type="monotone" dataKey="price" stroke="#00ffcc" dot={false} />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}
