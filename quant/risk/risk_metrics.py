from __future__ import annotations

import numpy as np
import pandas as pd


def value_at_risk(
    returns: pd.Series,
    confidence: float = 0.95,
) -> float:
    if not 0 < confidence < 1:
        raise ValueError("confidence must be between 0 and 1")
    return float(-returns.quantile(1.0 - confidence))


def expected_shortfall(
    returns: pd.Series,
    confidence: float = 0.95,
) -> float:
    var = value_at_risk(returns, confidence)
    tail = returns[returns <= -var]
    return float(-tail.mean()) if not tail.empty else var


def downside_deviation(
    returns: pd.Series,
    target: float = 0.0,
) -> float:
    downside = np.minimum(returns - target, 0.0)
    return float(np.sqrt(np.mean(downside ** 2)))
