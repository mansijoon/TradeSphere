import { useState } from "react";
import axios from "axios";

export default function OrderPanel({ prices, selectedSymbol }) {
  const [symbol, setSymbol] = useState(selectedSymbol || "AAPL");
  const [qty, setQty] = useState(1);
  const [price, setPrice] = useState("");

  const token = localStorage.getItem("token");

  const placeOrder = async (side) => {
    const orderPrice =
      Number(price) || prices?.[symbol];

    if (!orderPrice) {
      alert("Price not available");
      return;
    }

    try {
      await axios.post(
        `http://localhost:5000/api/orders/${side}`,
        {
          symbol,
          quantity: Number(qty),
          price: orderPrice
        },
        {
          headers: {
            Authorization: `Bearer ${token}`
          }
        }
      );

      console.log(
        `${side.toUpperCase()} order successful`
      );
    } catch (err) {
      console.log(
        err.response?.data || err.message
      );

      alert(
        err.response?.data?.error ||
        "Order failed"
      );
    }
  };

  return (
    <div style={styles.box}>
      <h3>Order Panel</h3>

      <input
        value={symbol}
        onChange={(e) =>
          setSymbol(e.target.value)
        }
        placeholder="Symbol"
        style={styles.input}
      />

      <input
        value={qty}
        onChange={(e) =>
          setQty(e.target.value)
        }
        type="number"
        placeholder="Quantity"
        style={styles.input}
      />

      <input
        value={price}
        onChange={(e) =>
          setPrice(e.target.value)
        }
        type="number"
        placeholder="Limit Price"
        style={styles.input}
      />

      <button
        style={styles.buy}
        onClick={() => placeOrder("buy")}
      >
        BUY
      </button>

      <button
        style={styles.sell}
        onClick={() => placeOrder("sell")}
      >
        SELL
      </button>
    </div>
  );
}

const styles = {
  box: {
    background: "#141a24",
    padding: 12,
    borderRadius: 10
  },

  input: {
    width: "100%",
    marginBottom: 8,
    padding: 8,
    background: "#0f1115",
    border: "1px solid #333",
    color: "white"
  },

  buy: {
    width: "48%",
    background: "green",
    color: "white",
    padding: 8,
    marginRight: "4%"
  },

  sell: {
    width: "48%",
    background: "red",
    color: "white",
    padding: 8
  }
};
