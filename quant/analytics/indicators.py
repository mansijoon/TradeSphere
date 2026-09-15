from __future__ import annotations

import pandas as pd


def sma(close: pd.Series, period: int) -> pd.Series:
    if period <= 0:
        raise ValueError("period must be positive")
    return close.rolling(period).mean()


def ema(close: pd.Series, period: int) -> pd.Series:
    if period <= 0:
        raise ValueError("period must be positive")
    return close.ewm(span=period, adjust=False).mean()


def returns(close: pd.Series) -> pd.Series:
    return close.pct_change().fillna(0.0)


def rolling_volatility(
    close: pd.Series,
    period: int = 20,
    annualization: int = 252,
) -> pd.Series:
    if period <= 0:
        raise ValueError("period must be positive")
    return returns(close).rolling(period).std() * (annualization ** 0.5)
