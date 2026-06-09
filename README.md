# 💳 E-Wallet Transaction System
First of all Open /Docs/
It contains all of endpoints already generated with sample of request and response

It just training repositories **microservices-based e-wallet system** built with **Java 26 + Spring Boot**, containerized with **Docker**, backed by **PostgreSQL**, and observable via **OpenTelemetry + Grafana LGTM**.

---

## 📐 Architecture Overview

The system is composed of **three independent Spring Boot microservices** that communicate over HTTP:

```
┌─────────────────────────────────────────────────────────────┐
│                        Docker Network                        │
│                                                             │
│  ┌──────────────────┐     ┌────────────────────────────┐   │
│  │   auth-service   │     │      wallet-service         │   │
│  │   port: 8080     │◄────│      port: 8081             │   │
│  │                  │     │  (validates token + sends   │   │
│  │  ┌────────────┐  │     │   notifications)            │   │
│  │  │  auth-db   │  │     │  ┌─────────────────────┐   │   │
│  │  │ (postgres) │  │     │  │     wallet-db        │   │   │
│  │  │ port: 5431 │  │     │  │    (postgres)        │   │   │
│  │  └────────────┘  │     │  │    port: 5433        │   │   │
│  └──────────────────┘     │  └─────────────────────┘   │   │
│                            └────────────┬───────────────┘   │
│                                         │                    │
│                            ┌────────────▼───────────────┐   │
│                            │   notification-service      │   │
│                            │   port: 8082               │   │
│                            └────────────────────────────┘   │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │   Grafana LGTM (OpenTelemetry Collector + Grafana)   │  │
│  │   Grafana UI: port 3000                              │  │
│  │   OTLP gRPC:  port 4317                              │  │
│  │   OTLP HTTP:  port 4318                              │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔄 Application Flow

Below is the end-to-end user journey through the system:

### 1. Register & Authenticate

```
Client ──POST /api/auth/register──► auth-service ──► auth-db
                                         │
                         returns { userId, status }

Client ──POST /api/auth/login────► auth-service ──► auth-db
                                         │
                         returns { token (JWT), expiresIn }
```

### 2. Create a Wallet

```
Client ──POST /api/wallet (Bearer token)──► wallet-service
                                                │
                              ┌─── validates token ───►  auth-service
                              │
                              └─── creates wallet in wallet-db
                                         │
                         returns { id, userId, balance, currency, ... }
```

### 3. Transfer Funds

```
Client ──POST /api/transaction/transfer (Bearer token)──► wallet-service
                                                               │
                                       ┌─── validates token ──► auth-service
                                       │
                                       ├─── locks sender & recipient wallets
                                       │    (pessimistic lock — SELECT FOR UPDATE
                                       │     in ascending wallet ID order to
                                       │     prevent deadlocks)
                                       │
                                       ├─── debits sender, credits recipient
                                       │
                                       ├─── persists Transaction record
                                       │    (type: TRANSFER_OUT, status: SUCCESS)
                                       │
                                       └─── async notification ──► notification-service
                                                                        │
                                                      logs notification to notification-db

                    returns { status, referenceNumber, amount,
                               senderWalletId, recipientWalletId, timestamp }
```

### 4. View Monthly Transaction History

```
Client ──GET /api/transaction/monthly?month=6&year=2026──► wallet-service
                                                                │
                                        ┌─── validates token ──► auth-service
                                        │
                                        ├─── queries wallet-db (native SQL)
                                        │
                                        └─── Java Streams post-processing:
                                              · resolves TRANSFER_IN/TRANSFER_OUT
                                                from the caller's perspective
                                              · filters by type, status, minAmount
                                              · aggregates totalVolume & volumeByType

                    returns { walletId, totalVolume, currency,
                               transactions[], volumeByType{} }
```

---

## 🛠️ Tech Stack

| Layer              | Technology                              |
|--------------------|-----------------------------------------|
| Language           | Java 26                                 |
| Framework          | Spring Boot 4.0.6                       |
| Service Comm.      | Spring Cloud OpenFeign 2025.1.1         |
| Database           | PostgreSQL 16 (per-service)             |
| Containerization   | Docker + Docker Compose                 |
| Observability      | OpenTelemetry + Grafana LGTM            |
| Security           | JWT (HS384) via jjwt 0.12.5             |
| Concurrency        | Pessimistic Locking (SELECT FOR UPDATE) |
| Data Processing    | Java Stream API                         |
| Boilerplate        | Lombok                                  |

---

## 🚀 Running the Application

### Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running
- No local Java or Maven installation required — everything runs inside containers

### Steps

1. **Open a terminal** and navigate to the project root:

   ```bash
   cd /path/to/transactions-monorepo
   ```

2. **Build and start all services** with a single command:

   ```bash
   docker compose up --build
   ```

   > ⚠️ The terminal must be opened inside the `transactions-monorepo` directory (the folder containing `compose.yaml`).

3. **Wait for all services to be healthy.** You'll see logs from each container. Services start in this dependency order:
   - `auth-db` and `wallet-db` (PostgreSQL) start first
   - `grafana-lgtm` starts in parallel
   - `auth-service` starts once `auth-db` is healthy
   - `wallet-service` starts once `wallet-db` and `auth-service` are started
   - `notification-service` starts once `wallet-db` is healthy

4. **Verify services are running:**

   | Service              | URL                                  |
   |----------------------|--------------------------------------|
   | Auth Service         | http://localhost:8080                |
   | Wallet Service       | http://localhost:8081                |
   | Notification Service | http://localhost:8082                |
   | Grafana Dashboard    | http://localhost:3000                |

5. **To stop all services:**

   ```bash
   docker compose down
   ```

   To also delete database volumes:

   ```bash
   docker compose down -v
   ```

---

## 🧪 Testing the API with Postman

A ready-to-use Postman collection is provided at:

```
Docs/Transaction Monorepo.postman_collection.json
```

### Import the Collection

1. Open **Postman**
2. Click **Import** → **File** → select `Docs/Transaction Monorepo.postman_collection.json`
3. The collection will appear as **"Transaction Monorepo"** with two folders:
   - `Auth Service` — Register, Login, Validate
   - `Wallet` — Create Wallet, Get Wallet, Transaction, Transaction History, etc.

### Set Up the Environment Variable

The **Login** request has a built-in Postman test script that automatically saves the JWT token:

```javascript
const response = pm.response.json();
pm.environment.set("token", response.token);
```

To use this:
1. Create a Postman **Environment** (e.g., `Local`)
2. Set it as the active environment before running requests

After logging in, `{{token}}` will be auto-populated for all subsequent requests.

---

### End-to-End Test Flow

Follow this sequence to test the complete transfer flow:

#### Step 1 — Register Sender

`Auth Service → Register`

```json
POST http://localhost:8080/api/auth/register
{
  "username": "sender",
  "email": "sender@gmail.com",
  "password": "Apaaja1"
}
```

#### Step 2 — Register Receiver

`Auth Service → Register` (change body to `receiver`)

```json
{
  "username": "receiver",
  "email": "receiver@gmail.com",
  "password": "Apaaja1"
}
```

#### Step 3 — Login as Sender

`Auth Service → login`

```json
POST http://localhost:8080/api/auth/login
{
  "username": "sender",
  "email": "sender@gmail.com",
  "password": "Apaaja1"
}
```

> ✅ The `token` environment variable is saved automatically.

#### Step 4 — Create Sender's Wallet

`Wallet → Create Wallet`

```
POST http://localhost:8081/api/wallet
Authorization: Bearer {{token}}
```

Note the returned wallet `id` — this is the **sender's walletId**.

#### Step 5 — Login as Receiver & Create Receiver's Wallet

Repeat Steps 3 and 4 using the receiver's credentials. Note the **receiver's walletId**.

#### Step 6 — Login as Sender Again & Transfer

`Wallet → Transaction`

```json
POST http://localhost:8081/api/transaction/transfer
Authorization: Bearer {{token}}   ← sender's token
{
  "recipientWalletId": <receiver's walletId>,
  "amount": 67500,
  "description": "makan makan"
}
```

#### Step 7 — Check Transaction History

`Wallet → Transaction History`

```
GET http://localhost:8081/api/transaction/monthly?month=6&year=2026
Authorization: Bearer {{token}}
```

**Optional filters:**

| Filter            | Example                                    |
|-------------------|--------------------------------------------|
| By type           | `?month=6&year=2026&type=TRANSFER_OUT`     |
| By min amount     | `?month=6&year=2026&minAmount=50000.00`    |
| By status         | `?month=6&year=2026&status=SUCCESS`        |
| Combined          | `?month=6&year=2026&type=TRANSFER_IN&minAmount=10000` |

---

## 📊 Observability (OpenTelemetry + Grafana)

All three services export traces, metrics, and logs via **OpenTelemetry** to the **Grafana LGTM** stack (Loki + Grafana + Tempo + Mimir), which is bundled as a single container.

- **Grafana Dashboard:** http://localhost:3000 (default credentials: `admin` / `admin`)
- Traces (Tempo), Logs (Loki), and Metrics (Prometheus/Mimir) are all available from the Grafana UI.

---

## 📂 Project Structure

```
transactions-monorepo/
├── auth-service/           # JWT-based authentication microservice
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── wallet-service/         # Wallet management & transfer microservice
│   ├── src/
│   ├── init.sql            # Creates notification_db on wallet-db container
│   ├── Dockerfile
│   └── pom.xml
├── notification-service/   # Notification microservice (email/SMS simulation)
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── Docs/
│   ├── API_DOCUMENTATION.md              # Full API contract (this system)
│   ├── Transaction Monorepo.postman_collection.json  # Postman collection
│   └── transaction-flow.png              # Flow diagram
└── compose.yaml            # Docker Compose — orchestrates all services
```

---

## 🗄️ Database Setup

Databases are automatically initialized by Docker Compose — no manual setup required.

| Database         | Container    | Host Port | Service          |
|------------------|--------------|-----------|------------------|
| `auth_db`        | `auth-db`    | `5431`    | auth-service     |
| `wallet_db`      | `wallet-db`  | `5433`    | wallet-service   |
| `notification_db`| `wallet-db`  | `5433`    | notification-service |

> `notification_db` is co-located on the `wallet-db` container and created via `wallet/init.sql`.
