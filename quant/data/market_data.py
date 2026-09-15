from __future__ import annotations

from dataclasses import dataclass

import pandas as pd


REQUIRED_COLUMNS = ("timestamp", "open", "high", "low", "close", "volume")


@dataclass(frozen=True)
class OHLCVData:
    frame: pd.DataFrame

    def __post_init__(self) -> None:
        missing = [c for c in REQUIRED_COLUMNS if c not in self.frame.columns]
        if missing:
            raise ValueError(f"Missing OHLCV columns: {missing}")

        frame = self.frame.copy()
        frame["timestamp"] = pd.to_datetime(frame["timestamp"], utc=True)
        frame = frame.sort_values("timestamp").drop_duplicates("timestamp")
        frame = frame.reset_index(drop=True)

        for column in ("open", "high", "low", "close", "volume"):
            frame[column] = pd.to_numeric(frame[column], errors="raise")

        if (frame["close"] <= 0).any():
            raise ValueError("Close prices must be positive")

        object.__setattr__(self, "frame", frame)

    @classmethod
    def from_csv(cls, path: str) -> "OHLCVData":
        return cls(pd.read_csv(path))

    def to_frame(self) -> pd.DataFrame:
        return self.frame.copy()
