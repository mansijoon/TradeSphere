CREATE TYPE account_status AS ENUM (
    'ACTIVE',
    'SUSPENDED',
    'CLOSED'
);

CREATE TYPE instrument_status AS ENUM (
    'ACTIVE',
    'HALTED',
    'DELISTED'
);

CREATE TYPE asset_type AS ENUM (
    'EQUITY',
    'ETF',
    'CRYPTO',
    'FUTURE'
);

CREATE TYPE order_side AS ENUM (
    'BUY',
    'SELL'
);

CREATE TYPE order_type AS ENUM (
    'MARKET',
    'LIMIT'
);

CREATE TYPE time_in_force AS ENUM (
    'DAY',
    'GTC',
    'IOC',
    'FOK'
);

CREATE TYPE order_status AS ENUM (
    'CREATED',
    'PENDING_RISK',
    'REJECTED',
    'ACCEPTED',
    'ROUTED',
    'PARTIALLY_FILLED',
    'FILLED',
    'CANCELLED',
    'EXPIRED'
);
