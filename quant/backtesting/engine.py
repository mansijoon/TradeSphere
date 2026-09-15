from __future__ import annotations

from dataclasses import dataclass

import pandas as pd

from analytics.metrics import PerformanceMetrics, calculate_metrics
from strategies.base import Strategy


@dataclass(frozen=True)
class BacktestConfig:
    initial_capital: float = 100_000.0
    commission_bps: float = 1.0
    slippage_bps: float = 2.0
    position_fraction: float = 1.0

    def __post_init__(self) -> None:
        if self.initial_capital <= 0:
            raise ValueError("initial_capital must be positive")
        if self.commission_bps < 0 or self.slippage_bps < 0:
            raise ValueError("Trading costs cannot be negative")
        if not 0 < self.position_fraction <= 1:
            raise ValueError("position_fraction must be in (0, 1]")


@dataclass
class BacktestResult:
    equity_curve: pd.Series
    trades: pd.DataFrame
    metrics: PerformanceMetrics


class BacktestEngine:
    def __init__(self, config: BacktestConfig | None = None):
        self.config = config or BacktestConfig()

    def run(self, data: pd.DataFrame, strategy: Strategy) -> BacktestResult:
        required = {"timestamp", "close"}
        missing = required - set(data.columns)
        if missing:
            raise ValueError(f"Missing columns: {sorted(missing)}")

        frame = data.copy().reset_index(drop=True)
        frame["signal"] = strategy.generate_signals(frame).fillna(0).astype(int)

        cash = self.config.initial_capital
        shares = 0.0
        previous_signal = 0

        equity = []
        trade_records = []

        for i, row in frame.iterrows():
            price = float(row["close"])
            signal = int(row["signal"])

            if signal != previous_signal:
                target_shares = (
                    signal
                    * self.config.position_fraction
                    * self.config.initial_capital
                    / price
                )

                delta = target_shares - shares

                if delta != 0:
                    direction = 1 if delta > 0 else -1
                    execution_price = price * (
                        1 + direction * self.config.slippage_bps / 10_000
                    )

                    notional = abs(delta) * execution_price
                    commission = notional * self.config.commission_bps / 10_000

                    cash -= delta * execution_price
                    cash -= commission

                    trade_records.append(
                        {
                            "timestamp": row["timestamp"],
                            "side": "BUY" if delta > 0 else "SELL",
                            "quantity": abs(delta),
                            "price": execution_price,
                            "notional": notional,
                            "commission": commission,
                            "pnl": 0.0,
                        }
                    )

                    shares = target_shares

                previous_signal = signal

            equity.append(cash + shares * price)

        equity_curve = pd.Series(
            equity,
            index=pd.to_datetime(frame["timestamp"], utc=True),
            name="equity",
        )

        trades = pd.DataFrame(trade_records)

        if not trades.empty:
            trades["pnl"] = trades["side"].map(
                {"BUY": -1.0, "SELL": 1.0}
            ) * trades["notional"] - trades["commission"]

        metrics = calculate_metrics(equity_curve, trades)

        return BacktestResult(
            equity_curve=equity_curve,
            trades=trades,
            metrics=metrics,
        )
