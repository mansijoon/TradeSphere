export default function PriceGrid({ prices, onSelect }) {
  return (
    <div style={styles.grid}>
      {Object.entries(prices).map(([symbol, price]) => (
        <div
          key={symbol}
          style={styles.card}
          onClick={() => onSelect?.(symbol)}
        >
          <div>{symbol}</div>
          <div style={styles.price}>${price.toFixed(2)}</div>
        </div>
      ))}
    </div>
  );
}

const styles = {
  grid: {
    display: "grid",
    gridTemplateColumns: "repeat(3, 1fr)",
    gap: "10px",
  },
  card: {
    background: "#1c2230",
    padding: "10px",
    borderRadius: "10px",
    cursor: "pointer",
  },
  price: {
    color: "#00ffcc",
    fontWeight: "bold",
  },
};
