class OrderBook {
  constructor() {
    this.buys = [];
    this.sells = [];
  }

  addOrder(order) {
    if (order.side === "BUY") {
      this.buys.push(order);
      this.buys.sort((a, b) => {
  if (a.price !== b.price) {
    return b.price - a.price;
  }

  return (
    new Date(a.createdAt) -
    new Date(b.createdAt)
  );
});
    } else {
      this.sells.push(order);
      this.sells.sort((a, b) => {
  if (a.price !== b.price) {
    return a.price - b.price;
  }

  return (
    new Date(a.createdAt) -
    new Date(b.createdAt)
  );
});
    }
  }

  match() {
    const trades = [];

    while (
      this.buys.length &&
      this.sells.length &&
      this.buys[0].price >= this.sells[0].price
    ) {
      const buy = this.buys[0];
      const sell = this.sells[0];

      const qty = Math.min(buy.remainingQuantity, sell.remainingQuantity);
      const price = sell.price;

      trades.push({ buy, sell, quantity: qty, price });

      buy.remainingQuantity -= qty;
      sell.remainingQuantity -= qty;

      if (buy.remainingQuantity === 0) this.buys.shift();
      if (sell.remainingQuantity === 0) this.sells.shift();
    }

    return trades;
  }

  removeOrder(orderId) {
    this.buys =
      this.buys.filter(
        o => String(o._id) !== String(orderId)
      );

    this.sells =
      this.sells.filter(
        o => String(o._id) !== String(orderId)
      );
  }


}

module.exports = new OrderBook();

