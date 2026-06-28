# TradeSphere Terminal

A production-grade, real-time trading simulation platform inspired by Zerodha, designed to replicate institutional trading infrastructure with a fully event-driven backend, custom order matching engine, and WebSocket-based live market and portfolio synchronization.

---

## Overview

TradeSphere Terminal is a full-stack trading system that simulates a brokerage-grade architecture with real-time financial data flow and deterministic trade execution.

It supports:

- Real-time BUY / SELL order execution
- Custom price-time priority matching engine
- Live market data streaming
- Event-driven portfolio and holdings updates
- WebSocket-based instant UI synchronization (no refresh required)

The system is designed around **event-driven consistency and real-time state propagation**.

---

## Core Features

### 1. Order Execution System
- BUY / SELL order processing
- Centralized validation and routing
- Service-layer based architecture
- Integrated with matching engine pipeline

---

### 2. Custom Matching Engine
- Price-time priority order book
- Deterministic trade execution logic
- Handles concurrent orders safely
- Emits atomic trade events on execution

---

### 3. Event-Driven Architecture

All system state changes are driven through events.

#### Core Events:
- `ORDER_CREATED`
- `TRADE_EXECUTED`
- `PORTFOLIO_UPDATE`

#### Flow:

Order → Matching Engine → Trade Execution → Event Bus → Services → WebSocket → UI

---

## 4. Real-Time Portfolio System

Portfolio is fully synchronized in real time and updated after every trade.

### Metrics:
- Cash balance
- Holdings (quantity + average price)
- Invested value
- Market value
- Unrealized PnL
- Realized PnL
- Total portfolio value

### Guarantees:
- Strong consistency after every trade
- Atomic updates via event handlers
- No manual refresh required

---

## 5. Market Data Engine

- Continuous price simulation
- Controlled volatility per asset
- WebSocket streaming of tick updates
- Optimized broadcast system

---

## 6. Holdings System

- Auto creation and updates of positions
- Weighted average price calculation
- Accurate multi-trade aggregation
- Fully synced with trade execution pipeline

---

## 7. WebSocket Real-Time Layer

- Live market price updates
- Portfolio snapshot updates after trades
- Multi-client broadcast system
- Fully event-driven UI updates

---

## System Architecture


Frontend (React)
        │
        │ WebSocket + REST API
        ▼
Backend (Node.js + Express)
        │
        ├── API Layer (Auth / Orders / Portfolio)
        ├── Service Layer (Business Logic)
        ├── Matching Engine (Order Book)
        ├── Event Bus (Core System)
        ├── Repositories (MongoDB Layer)
        └── MongoDB (Database)

WebSocket Layer
        ├── Market Data Stream
        ├── Portfolio Stream
        └── Trade Broadcast

---

## Execution Flow

1. User places BUY/SELL order  
2. Order enters matching engine  
3. Matching engine executes trade  
4. `TRADE_EXECUTED` event emitted  
5. Holding & Portfolio services update state  
6. WebSocket pushes updated snapshot  
7. Frontend updates instantly

---

## Engineering Highlights

- Event-driven architecture with decoupled services
- Deterministic trade execution via matching engine
- Real-time portfolio synchronization without polling
- Atomic updates for holdings and cash balance
- WebSocket-based full UI reactivity
- Scalable backend service separation

---

## Tech Stack

### Backend
- Node.js
- Express.js
- MongoDB + Mongoose
- WebSockets (`ws`)
- Event-driven architecture

### Frontend
- React (Vite)
- WebSocket client
- Real-time dashboard UI
- Charting components

---

## What This Project Demonstrates

- Real-time distributed system design
- Event-driven backend architecture
- Custom financial matching engine
- WebSocket-based streaming systems
- Scalable backend design patterns
- Production-style system thinking

---

## Future Enhancements

- Redis-based order book optimization
- Kafka-style event streaming system
- Horizontal scaling of WebSocket layer
- Risk management engine (margin/exposure)
- Time-series storage for historical data
- Docker + Kubernetes deployment

---

## Status

Fully functional real-time trading simulation system featuring:

- End-to-end order execution pipeline
- Custom matching engine
- Live market data streaming
- Event-driven portfolio synchronization
- WebSocket-based instant UI updates

---

## System Highlights

- Fully event-driven backend separating order execution from portfolio computation
- Custom matching engine implementing deterministic price-time priority execution
- Real-time WebSocket streaming for market data and portfolio state
- Strong consistency model maintained via event-driven updates
- Stateless service architecture enabling scalability

---

## Author

TradeSphere Terminal is built as a high-fidelity trading system simulation demonstrating backend engineering, distributed systems design, and real-time financial architecture.
