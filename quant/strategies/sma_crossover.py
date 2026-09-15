from __future__ import annotations

import pandas as pd

from analytics.indicators import sma
from strategies.base import Strategy


class SMACrossoverStrategy(Strategy):
    def __init__(self, fast_period: int = 20, slow_period: int = 50):
        if fast_period <= 0 or slow_period <= 0:
            raise ValueError("Periods must be positive")
        if fast_period >= slow_period:
            raise ValueError("fast_period must be less than slow_period")

        self.fast_period = fast_period
        self.slow_period = slow_period

    def generate_signals(self, data: pd.DataFrame) -> pd.Series:
        fast = sma(data["close"], self.fast_period)
        slow = sma(data["close"], self.slow_period)

        signal = pd.Series(0, index=data.index, dtype=int)
        signal.loc[fast > slow] = 1
        signal.loc[fast < slow] = -1

        return signal
