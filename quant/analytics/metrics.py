from __future__ import annotations

from dataclasses import dataclass

import numpy as np
import pandas as pd


@dataclass(frozen=True)
class PerformanceMetrics:
    total_return: float
    annualized_return: float
    annualized_volatility: float
    sharpe_ratio: float
    max_drawdown: float
    win_rate: float


def max_drawdown(equity: pd.Series) -> float:
    running_max = equity.cummax()
    drawdown = equity / running_max - 1.0
    return float(drawdown.min())


def calculate_metrics(
    equity: pd.Series,
    trades: pd.DataFrame | None = None,
    periods_per_year: int = 252,
    risk_free_rate: float = 0.0,
) -> PerformanceMetrics:
    equity = equity.dropna()

    if len(equity) < 2:
        raise ValueError("At least two equity observations are required")

    period_returns = equity.pct_change().dropna()

    total_return = float(equity.iloc[-1] / equity.iloc[0] - 1.0)

    years = (len(period_returns) / periods_per_year)
    annualized_return = (
        float((1.0 + total_return) ** (1.0 / years) - 1.0)
        if years > 0 and 1.0 + total_return > 0
        else -1.0
    )

    annualized_volatility = float(
        period_returns.std(ddof=1) * np.sqrt(periods_per_year)
    )

    excess = period_returns - risk_free_rate / periods_per_year
    sharpe_ratio = (
        float(excess.mean() / excess.std(ddof=1) * np.sqrt(periods_per_year))
        if excess.std(ddof=1) > 0
        else 0.0
    )

    win_rate = 0.0
    if trades is not None and not trades.empty:
        pnl = trades["pnl"]
        win_rate = float((pnl > 0).mean())

    return PerformanceMetrics(
        total_return=total_return,
        annualized_return=annualized_return,
        annualized_volatility=annualized_volatility,
        sharpe_ratio=sharpe_ratio,
        max_drawdown=max_drawdown(equity),
        win_rate=win_rate,
    )
