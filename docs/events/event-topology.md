# MarketForge Event Topology

## Topics

### order.commands
Spring Boot → Matching Engine

Carries:
- Order ID
- Account ID
- Instrument ID
- Side
- Order type
- Time in force
- Price
- Quantity

### order.events
Matching Engine → downstream services

Carries:
- Order accepted
- Order cancelled
- Order rejected
- Order partially filled
- Order filled

### trade.executed
Matching Engine → downstream services

Carries:
- Trade ID
- Taker order ID
- Maker order ID
- Instrument ID
- Execution price
- Execution quantity
- Timestamp

## Consumers

trade.executed:
- Portfolio Service
- Risk Service
- Market Data Service
- PostgreSQL persistence

order.events:
- Portfolio Service
- Trading Service
- WebSocket/API Gateway
