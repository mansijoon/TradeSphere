export default function Watchlist({ prices }) {
  return (
    <div style={styles.box}>
      <h3 style={styles.title}>Watchlist</h3>

      {Object.entries(prices).map(([symbol, price]) => (
        <div key={symbol} style={styles.row}>
          <span>{symbol}</span>
          <span style={styles.price}>${price.toFixed(2)}</span>
        </div>
      ))}
    </div>
  );
}

const styles = {
  box: {
    background: "#141a24",
    padding: "12px",
    borderRadius: "10px",
    height: "100%",
  },
  title: { marginBottom: "10px", color: "#aaa" },
  row: {
    display: "flex",
    justifyContent: "space-between",
    padding: "6px 0",
    borderBottom: "1px solid #222",
    color: "white",
  },
  price: { color: "#00ffcc" },
};
