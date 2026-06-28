export default function HoldingsTable({ holdings }) {
  if (!holdings || holdings.length === 0) {
    return <div style={{ color: "#888" }}>No holdings</div>;
  }

  return (
    <table style={styles.table}>
      <thead>
        <tr style={{ color: "#aaa" }}>
          <th>Symbol</th>
          <th>Qty</th>
          <th>Avg</th>
          <th>Current</th>
          <th>PnL</th>
        </tr>
      </thead>

      <tbody>
        {holdings.map((h) => (
          <tr key={h.symbol}>
            <td>{h.symbol}</td>
            <td>{h.quantity}</td>
            <td>{Number(h.averagePrice || 0).toFixed(2)}</td>
            <td>{Number(h.currentPrice || 0).toFixed(2)}</td>
            <td
              style={{
                color:
                  Number(h.profitLoss || 0) >= 0
                    ? "#00ff88"
                    : "#ff4d4d",
              }}
            >
              {Number(h.profitLoss || 0).toFixed(2)}
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

const styles = {
  table: {
    width: "100%",
    marginTop: "10px",
    color: "white",
    borderCollapse: "collapse",
  },
};
