import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
} from "recharts";

export default function ChartPanel({ data, symbol }) {
  return (
    <div style={styles.box}>
      <ResponsiveContainer width="100%" height={250}>
        <LineChart data={data}>
          <XAxis dataKey="time" hide />
          <YAxis domain={["auto", "auto"]} />
          <Tooltip />
          <Line type="monotone" dataKey="price" stroke="#00ffcc" />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}

const styles = {
  box: {
    background: "#141a24",
    padding: "10px",
    borderRadius: "10px",
  },
};
