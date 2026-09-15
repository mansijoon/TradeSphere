import numpy as np
import pandas as pd
import pytest

from analytics.indicators import ema, sma
from analytics.metrics import calculate_metrics, max_drawdown
from backtesting.engine import BacktestConfig, BacktestEngine
from data.market_data import OHLCVData
from risk.risk_metrics import expected_shortfall, value_at_risk
from strategies.sma_crossover import SMACrossoverStrategy


def sample_data(n=100):
    timestamps = pd.date_range("2026-01-01", periods=n, freq="D")
    close = pd.Series(np.linspace(100, 150, n))

    return pd.DataFrame(
        {
            "timestamp": timestamps,
            "open": close,
            "high": close + 1,
            "low": close - 1,
            "close": close,
            "volume": 1000,
        }
    )


def test_ohlcv_validation():
    data = OHLCVData(sample_data())
    assert len(data.frame) == 100


def test_sma():
    values = pd.Series([1, 2, 3, 4, 5])
    result = sma(values, 3)
    assert result.iloc[-1] == 4


def test_ema():
    values = pd.Series([1, 2, 3, 4, 5])
    result = ema(values, 3)
    assert len(result) == 5
    assert result.iloc[-1] > result.iloc[0]


def test_max_drawdown():
    equity = pd.Series([100, 120, 90, 110])
    assert max_drawdown(equity) == -0.25


def test_metrics():
    equity = pd.Series([100, 101, 102, 104, 106])
    metrics = calculate_metrics(equity)
    assert metrics.total_return == pytest.approx(0.06)
    assert metrics.max_drawdown == 0.0


def test_risk_metrics():
    returns = pd.Series([-0.10, -0.05, 0.01, 0.02, 0.03])
    assert value_at_risk(returns, 0.8) >= 0
    assert expected_shortfall(returns, 0.8) >= 0


def test_sma_crossover_strategy():
    data = sample_data()
    strategy = SMACrossoverStrategy(5, 20)
    signals = strategy.generate_signals(data)
    assert len(signals) == len(data)
    assert signals.iloc[-1] == 1


def test_backtest():
    data = sample_data()
    strategy = SMACrossoverStrategy(5, 20)

    result = BacktestEngine(
        BacktestConfig(
            initial_capital=100_000,
            commission_bps=1,
            slippage_bps=2,
        )
    ).run(data, strategy)

    assert len(result.equity_curve) == len(data)
    assert result.equity_curve.iloc[-1] > 0
    assert result.metrics.total_return > 0
