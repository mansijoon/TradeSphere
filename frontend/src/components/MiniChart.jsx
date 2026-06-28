import {
  LineChart,
  Line,
  ResponsiveContainer,
} from "recharts";

export default function MiniChart({ data }) {
  return (
    <ResponsiveContainer width="100%" height={50}>
      <LineChart data={data}>
        <Line
          type="monotone"
          dataKey="price"
          stroke="#00ffcc"
          dot={false}
          strokeWidth={2}
        />
      </LineChart>
    </ResponsiveContainer>
  );
}
