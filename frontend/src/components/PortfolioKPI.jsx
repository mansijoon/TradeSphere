import React from "react";

export default function PortfolioKPI({ portfolio }) {
  const safe = {
    totalValue: portfolio?.totalPortfolioValue ?? 0,
    invested: portfolio?.investedValue ?? 0,
    cash: portfolio?.cashBalance ?? 0,
    pnl: portfolio?.totalPnL ?? 0,
  };

  return (
    <div style={styles.row}>
      <div style={styles.card}>
        <h4>Total Value</h4>
        <p>${safe.totalValue.toFixed(2)}</p>
      </div>

      <div style={styles.card}>
        <h4>Invested</h4>
        <p>${safe.invested.toFixed(2)}</p>
      </div>

      <div style={styles.card}>
        <h4>Cash</h4>
        <p>${safe.cash.toFixed(2)}</p>
      </div>

      <div style={styles.card}>
        <h4>PnL</h4>
        <p style={{ color: safe.pnl >= 0 ? "lime" : "red" }}>
          ${safe.pnl.toFixed(2)}
        </p>
      </div>
    </div>
  );
}

const styles = {
  row: {
    display: "grid",
    gridTemplateColumns: "repeat(4, 1fr)",
    gap: "10px",
    marginBottom: "15px",
  },
  card: {
    background: "#151b26",
    padding: "10px",
    borderRadius: "10px",
    color: "white",
  },
};
