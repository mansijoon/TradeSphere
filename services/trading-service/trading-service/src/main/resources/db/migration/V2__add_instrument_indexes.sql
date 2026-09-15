CREATE INDEX IF NOT EXISTS idx_instruments_symbol
    ON instruments(symbol);

CREATE INDEX IF NOT EXISTS idx_instruments_status
    ON instruments(status);
