# MarketForge System Architecture

## Frontend

React + TypeScript provides the real-time trading terminal.

## API Gateway

Node.js + Express provides:

- REST APIs
- WebSocket connections
- Request validation
- API aggregation
- Authentication boundary
- Rate limiting

## Core Backend

Spring Boot provides the trading business domain:

- Orders
- Accounts
- Trading
- Risk
- Positions
- Portfolios
- Market services

## Matching Engine

C++20 provides:

- Order book
- Order matching
- Price-time priority
- Deterministic execution
- Concurrency
- Performance-critical processing

## Event Backbone

Kafka provides asynchronous event-driven communication.

## Data Layer

PostgreSQL is the authoritative durable datastore.

Redis provides hot state, caching and rate limiting.

## Quantitative Engine

Python provides:

- Backtesting
- Strategies
- Analytics
- Performance metrics
- Quantitative risk calculations

## Deployment

Docker provides container packaging.

Kubernetes provides orchestration.

Helm provides repeatable Kubernetes deployments.

AWS is the cloud deployment target.
