ALTER TABLE trades
    ADD COLUMN external_trade_id VARCHAR(64);

UPDATE trades
SET external_trade_id = id::text
WHERE external_trade_id IS NULL;

ALTER TABLE trades
    ALTER COLUMN external_trade_id SET NOT NULL;

ALTER TABLE trades
    ADD CONSTRAINT unique_trade_external_id
        UNIQUE (instrument_id, external_trade_id);

CREATE INDEX idx_trades_external_trade_id
    ON trades(external_trade_id);
