BEGIN;

CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE trading_accounts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    currency CHAR(3) NOT NULL DEFAULT 'USD',
    cash_balance NUMERIC(20, 8) NOT NULL DEFAULT 0,
    available_balance NUMERIC(20, 8) NOT NULL DEFAULT 0,
    reserved_balance NUMERIC(20, 8) NOT NULL DEFAULT 0,
    status account_status NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT positive_cash_balance
        CHECK (cash_balance >= 0),

    CONSTRAINT positive_available_balance
        CHECK (available_balance >= 0),

    CONSTRAINT positive_reserved_balance
        CHECK (reserved_balance >= 0)
);

CREATE INDEX idx_trading_accounts_user_id
    ON trading_accounts(user_id);

CREATE TABLE instruments (
    id UUID PRIMARY KEY,
    symbol VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    asset_type asset_type NOT NULL,
    tick_size NUMERIC(20, 8) NOT NULL,
    lot_size NUMERIC(20, 8) NOT NULL,
    status instrument_status NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT positive_tick_size
        CHECK (tick_size > 0),

    CONSTRAINT positive_lot_size
        CHECK (lot_size > 0)
);

CREATE TABLE orders (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL REFERENCES trading_accounts(id),
    instrument_id UUID NOT NULL REFERENCES instruments(id),
    client_order_id VARCHAR(64) NOT NULL,
    side order_side NOT NULL,
    order_type order_type NOT NULL,
    time_in_force time_in_force NOT NULL DEFAULT 'DAY',
    price NUMERIC(20, 8),
    quantity NUMERIC(20, 8) NOT NULL,
    filled_quantity NUMERIC(20, 8) NOT NULL DEFAULT 0,
    remaining_quantity NUMERIC(20, 8) NOT NULL,
    status order_status NOT NULL DEFAULT 'CREATED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT positive_order_quantity
        CHECK (quantity > 0),

    CONSTRAINT non_negative_filled_quantity
        CHECK (filled_quantity >= 0),

    CONSTRAINT valid_remaining_quantity
        CHECK (remaining_quantity >= 0 AND remaining_quantity <= quantity),

    CONSTRAINT limit_orders_require_price
        CHECK (
            order_type = 'MARKET'
            OR price IS NOT NULL
        ),

    CONSTRAINT positive_limit_price
        CHECK (
            price IS NULL
            OR price > 0
        ),

    CONSTRAINT unique_client_order
        UNIQUE (account_id, client_order_id)
);

CREATE INDEX idx_orders_account_id
    ON orders(account_id);

CREATE INDEX idx_orders_instrument_id
    ON orders(instrument_id);

CREATE INDEX idx_orders_status
    ON orders(status);

CREATE INDEX idx_orders_created_at
    ON orders(created_at);

CREATE TABLE trades (
    id UUID PRIMARY KEY,
    instrument_id UUID NOT NULL REFERENCES instruments(id),
    buy_order_id UUID NOT NULL REFERENCES orders(id),
    sell_order_id UUID NOT NULL REFERENCES orders(id),
    price NUMERIC(20, 8) NOT NULL,
    quantity NUMERIC(20, 8) NOT NULL,
    executed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT positive_trade_price
        CHECK (price > 0),

    CONSTRAINT positive_trade_quantity
        CHECK (quantity > 0)
);

CREATE INDEX idx_trades_instrument_time
    ON trades(instrument_id, executed_at DESC);

CREATE INDEX idx_trades_buy_order
    ON trades(buy_order_id);

CREATE INDEX idx_trades_sell_order
    ON trades(sell_order_id);

CREATE TABLE positions (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL REFERENCES trading_accounts(id),
    instrument_id UUID NOT NULL REFERENCES instruments(id),
    quantity NUMERIC(20, 8) NOT NULL DEFAULT 0,
    average_entry_price NUMERIC(20, 8) NOT NULL DEFAULT 0,
    realized_pnl NUMERIC(20, 8) NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    UNIQUE (account_id, instrument_id)
);

CREATE TABLE portfolio_snapshots (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL REFERENCES trading_accounts(id),
    total_equity NUMERIC(20, 8) NOT NULL,
    cash NUMERIC(20, 8) NOT NULL,
    market_value NUMERIC(20, 8) NOT NULL,
    unrealized_pnl NUMERIC(20, 8) NOT NULL,
    realized_pnl NUMERIC(20, 8) NOT NULL,
    recorded_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_portfolio_snapshots_account_time
    ON portfolio_snapshots(account_id, recorded_at DESC);

CREATE TABLE market_ticks (
    id BIGSERIAL PRIMARY KEY,
    instrument_id UUID NOT NULL REFERENCES instruments(id),
    price NUMERIC(20, 8) NOT NULL,
    quantity NUMERIC(20, 8) NOT NULL,
    bid_price NUMERIC(20, 8),
    ask_price NUMERIC(20, 8),
    timestamp TIMESTAMPTZ NOT NULL,

    CONSTRAINT positive_tick_price
        CHECK (price > 0),

    CONSTRAINT positive_tick_quantity
        CHECK (quantity > 0)
);

CREATE INDEX idx_market_ticks_instrument_time
    ON market_ticks(instrument_id, timestamp DESC);

COMMIT;
