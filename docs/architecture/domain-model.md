# MarketForge Domain Model

## Core Entities

### User

Represents a MarketForge account owner.

Attributes:

- id
- username
- email
- password_hash
- status
- created_at
- updated_at

### Trading Account

Represents a paper-trading account owned by a user.

Attributes:

- id
- user_id
- currency
- cash_balance
- available_balance
- reserved_balance
- status
- created_at
- updated_at

### Instrument

Represents a tradeable financial instrument.

Attributes:

- id
- symbol
- name
- asset_type
- tick_size
- lot_size
- status
- created_at

### Order

Represents an instruction submitted by a trader.

Attributes:

- id
- account_id
- instrument_id
- client_order_id
- side
- order_type
- time_in_force
- price
- quantity
- filled_quantity
- remaining_quantity
- status
- created_at
- updated_at

### Trade

Represents an executed match between orders.

Attributes:

- id
- instrument_id
- buy_order_id
- sell_order_id
- price
- quantity
- executed_at

### Position

Represents an account's current holding in an instrument.

Attributes:

- id
- account_id
- instrument_id
- quantity
- average_entry_price
- realized_pnl
- updated_at

### Portfolio Snapshot

Represents a point-in-time valuation of an account.

Attributes:

- id
- account_id
- total_equity
- cash
- market_value
- unrealized_pnl
- realized_pnl
- recorded_at

### Market Tick

Represents a market price update.

Attributes:

- id
- instrument_id
- price
- quantity
- bid_price
- ask_price
- timestamp

## Relationships

User
  |
  └── Trading Account
          |
          ├── Orders
          |
          ├── Positions
          |
          └── Portfolio Snapshots

Instrument
  |
  ├── Orders
  ├── Trades
  ├── Positions
  └── Market Ticks

Order
  |
  └── Trade

## Order Lifecycle

CREATED
    |
    v
PENDING_RISK
    |
    +------> REJECTED
    |
    v
ACCEPTED
    |
    v
ROUTED
    |
    v
PARTIALLY_FILLED
    |
    v
FILLED

Any active order may also transition to:

CANCELLED
EXPIRED
