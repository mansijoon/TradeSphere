from __future__ import annotations

import numpy as np
import pandas as pd

from backtesting.engine import BacktestConfig, BacktestEngine
from strategies.sma_crossover import SMACrossoverStrategy


def generate_demo_data(n: int = 500) -> pd.DataFrame:
    rng = np.random.default_rng(42)
    timestamps = pd.date_range("2025-01-01", periods=n, freq="D")

    returns = rng.normal(0.0005, 0.012, n)
    close = 100 * np.exp(np.cumsum(returns))

    return pd.DataFrame(
        {
            "timestamp": timestamps,
            "open": close * (1 + rng.normal(0, 0.002, n)),
            "high": close * (1 + abs(rng.normal(0, 0.005, n))),
            "low": close * (1 - abs(rng.normal(0, 0.005, n))),
            "close": close,
            "volume": rng.integers(100_000, 1_000_000, n),
        }
    )


if __name__ == "__main__":
    data = generate_demo_data()

    result = BacktestEngine(
        BacktestConfig(
            initial_capital=100_000,
            commission_bps=1,
            slippage_bps=2,
        )
    ).run(
        data,
        SMACrossoverStrategy(fast_period=20, slow_period=50),
    )

    print("\n=== MarketForge Quant Backtest ===")
    print(f"Initial Capital:       $100,000.00")
    print(f"Final Equity:          ${result.equity_curve.iloc[-1]:,.2f}")
    print(f"Total Return:          {result.metrics.total_return:.2%}")
    print(f"Annualized Return:     {result.metrics.annualized_return:.2%}")
    print(f"Annualized Volatility: {result.metrics.annualized_volatility:.2%}")
    print(f"Sharpe Ratio:          {result.metrics.sharpe_ratio:.3f}")
    print(f"Maximum Drawdown:      {result.metrics.max_drawdown:.2%}")
    print(f"Win Rate:              {result.metrics.win_rate:.2%}")
    print(f"Trades:                {len(result.trades)}")
