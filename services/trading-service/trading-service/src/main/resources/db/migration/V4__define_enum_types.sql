DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'account_status') THEN
        CREATE TYPE account_status AS ENUM (
            'ACTIVE',
            'SUSPENDED',
            'CLOSED'
        );
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'asset_type') THEN
        CREATE TYPE asset_type AS ENUM (
            'EQUITY',
            'ETF',
            'CRYPTO',
            'FUTURE'
        );
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'instrument_status') THEN
        CREATE TYPE instrument_status AS ENUM (
            'ACTIVE',
            'HALTED',
            'DELISTED'
        );
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'order_side') THEN
        CREATE TYPE order_side AS ENUM (
            'BUY',
            'SELL'
        );
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'order_type') THEN
        CREATE TYPE order_type AS ENUM (
            'MARKET',
            'LIMIT'
        );
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'time_in_force') THEN
        CREATE TYPE time_in_force AS ENUM (
            'DAY',
            'GTC',
            'IOC',
            'FOK'
        );
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'order_status') THEN
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
    END IF;
END
$$;
