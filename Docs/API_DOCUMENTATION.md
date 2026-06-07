# 🌐 API Documentation: E-Wallet Transaction System

This document describes the REST API contract for the three microservices in the system. All endpoints, request/response formats, and HTTP status codes reflect the current implementation.

---

## 🔑 Authentication Service (`auth-service`)

- **Default Port:** `8080`
- **Base Path:** `/api/auth`

### 1. Register User

Create a new user account.

- **Method:** `POST`
- **URL:** `/api/auth/register`
- **Content-Type:** `application/json`

**Request Body:**
```json
{
  "username": "sender",
  "email": "sender@gmail.com",
  "password": "Apaaja1"
}
```

**Response (`201 Created`):**
```json
{
  "message": "User registered successfully",
  "userId": 8,
  "status": "success"
}
```

**Response (`400 Bad Request` — Username/Email already exists):**
```json
{
  "status": "error",
  "message": "Username or Email already exists"
}
```

---

### 2. Login User

Authenticate and receive a JWT access token.

- **Method:** `POST`
- **URL:** `/api/auth/login`
- **Content-Type:** `application/json`

**Request Body:**
```json
{
  "username": "sender",
  "email": "sender@gmail.com",
  "password": "Apaaja1"
}
```

> **Note:** The `login` endpoint's Postman test script automatically saves the token as the `token` environment variable. Subsequent wallet/transaction requests use `{{token}}`.

**Response (`200 OK`):**
```json
{
  "expiresIn": 3600,
  "status": "success",
  "token": "eyJhbGciOiJIUzM4NCJ9..."
}
```

**Response (`401 Unauthorized` — Invalid credentials):**
```json
{
  "status": "error",
  "message": "Invalid username or password"
}
```

---

### 3. Validate Token

Verify a JWT token and extract user details. Called internally by other microservices (wallet → auth-service).

- **Method:** `GET`
- **URL:** `/api/auth/validate`
- **Headers:**
  - `Authorization: Bearer <token>`

**Response (`200 OK`):**
```json
{
  "valid": true,
  "role": "USER",
  "userId": 8,
  "email": "sender@gmail.com",
  "username": "sender"
}
```

**Response (`401 Unauthorized` — Token expired or invalid):**
```json
{
  "valid": false,
  "message": "JWT Token has expired or is invalid"
}
```

---

## 💼 Wallet Service (`wallet`)

- **Default Port:** `8081`
- **Base Path:** `/api`
- **Security:** All endpoints require the `Authorization: Bearer <token>` header. The service validates it by calling `auth-service`'s validate endpoint.

### 1. Create Wallet

Create a wallet for the authenticated user. A new wallet is initialized with a starting balance.

- **Method:** `POST`
- **URL:** `http://localhost:8081/api/wallet`
- **Headers:**
  - `Authorization: Bearer <token>`
- **Request Body:** _(empty)_

**Response (`201 Created`):**
```json
{
  "id": 4,
  "userId": 8,
  "balance": 100000,
  "currency": "IDR",
  "createdAt": "2026-06-07T11:26:44.121620620Z",
  "updatedAt": "2026-06-07T11:26:44.121621806Z"
}
```

---

### 2. Get Wallet

Fetch wallet details and balance for the authenticated user.

- **Method:** `GET`
- **URL:** `http://localhost:8081/api/wallet`
- **Headers:**
  - `Authorization: Bearer <token>`

**Response (`200 OK`):**
```json
{
  "id": 4,
  "userId": 8,
  "balance": 100000,
  "currency": "IDR",
  "createdAt": "2026-06-07T11:26:44.121621Z",
  "updatedAt": "2026-06-07T11:26:44.121622Z"
}
```

---

### 3. Transfer Funds

Initiate a money transfer to another wallet. Uses **pessimistic locking** (`SELECT FOR UPDATE` in ascending wallet ID order) to prevent race conditions and deadlocks.

- **Method:** `POST`
- **URL:** `http://localhost:8081/api/transaction/transfer`
- **Headers:**
  - `Authorization: Bearer <token>`
- **Content-Type:** `application/json`

**Request Body:**
```json
{
  "recipientWalletId": 5,
  "amount": 67500,
  "description": "makan makan"
}
```

**Response (`200 OK` — Success):**
```json
{
  "status": "SUCCESS",
  "referenceNumber": "TX-20260607-A65697",
  "amount": 67500,
  "senderWalletId": 4,
  "recipientWalletId": 5,
  "description": "makan makan",
  "timestamp": "2026-06-07T11:30:16.559330493Z"
}
```

**Response (`400 Bad Request` — Insufficient balance):**
```json
{
  "message": "Insufficient balance for this transaction",
  "status": "FAILED"
}
```

**Response (`400 Bad Request` — Self-transfer):**
```json
{
  "message": "Cannot transfer to own wallet",
  "status": "FAILED"
}
```

**Response (`404 Not Found` — Recipient wallet does not exist):**
```json
{
  "message": "Recipient wallet not found",
  "status": "FAILED"
}
```

> **Side Effect:** On a successful transfer, the wallet service asynchronously calls the notification service to send an email notification to the recipient.

---

### 4. Monthly Transaction History

Retrieve the wallet's transaction history for a specific month/year, filtered and aggregated in-memory using **Java Streams**.

- **Method:** `GET`
- **URL:** `http://localhost:8081/api/transaction/monthly`
- **Headers:**
  - `Authorization: Bearer <token>`
- **Query Parameters:**

| Parameter   | Type     | Required | Default | Description                                      |
|-------------|----------|----------|---------|--------------------------------------------------|
| `month`     | `int`    | No       | `6`     | Month number (1–12)                              |
| `year`      | `int`    | No       | `2026`  | 4-digit year                                     |
| `type`      | `String` | No       | —       | Filter by type: `TRANSFER_IN` or `TRANSFER_OUT`  |
| `status`    | `String` | No       | —       | Filter by status: `SUCCESS` or `FAILED`          |
| `minAmount` | `Double` | No       | —       | Filter transactions ≥ this amount                |

**Response (`200 OK` — All transactions for month/year):**
```json
{
  "walletId": 4,
  "totalVolume": 67500,
  "currency": "IDR",
  "transactions": [
    {
      "id": 3,
      "walletId": 4,
      "counterpartyWalletId": 5,
      "recipientWalletId": null,
      "amount": 67500,
      "type": "TRANSFER_OUT",
      "status": "SUCCESS",
      "referenceNumber": "TX-20260607-A65697",
      "description": "makan makan",
      "createdAt": "2026-06-07T11:30:16.559330Z"
    }
  ],
  "volumeByType": {
    "TRANSFER_OUT": 67500
  }
}
```

**Response (`200 OK` — Filtered by `type=TRANSFER_IN`):**
```json
{
  "walletId": 5,
  "totalVolume": 67500,
  "currency": "IDR",
  "transactions": [
    {
      "id": 3,
      "walletId": 5,
      "counterpartyWalletId": 4,
      "recipientWalletId": null,
      "amount": 67500,
      "type": "TRANSFER_IN",
      "status": "SUCCESS",
      "referenceNumber": "TX-20260607-A65697",
      "description": "makan makan",
      "createdAt": "2026-06-07T11:30:16.559330Z"
    }
  ],
  "volumeByType": {
    "TRANSFER_IN": 67500
  }
}
```

> **Note on `type` field:** The `type` is resolved from the authenticated user's perspective. If the user's wallet is the counterparty (recipient), the type is returned as `TRANSFER_IN` even though the underlying record is stored as `TRANSFER_OUT`.

---

## 🔔 Notification Service (`notification`)

- **Default Port:** `8082`
- **Base Path:** `/api/notifications`

### 1. Send Notification

Internal endpoint invoked by the `wallet` service after a successful transfer.

- **Method:** `POST`
- **URL:** `http://localhost:8082/api/notifications/send`
- **Content-Type:** `application/json`

**Request Body:**
```json
{
  "userId": 9,
  "type": "EMAIL",
  "message": "You have received IDR 67500.00 from User 8."
}
```

**Response (`200 OK`):**
```json
{
  "status": "sent",
  "notificationId": 9001
}
```

> This endpoint is not intended to be called directly by clients. It is triggered internally as a side effect of the `/api/transaction/transfer` endpoint.

---

## 🔗 Service Dependency Map

```
Client
  │
  ├─► auth-service  (port 8080)  ◄─── validates token for other services
  │
  ├─► wallet-service (port 8081)
  │       │
  │       ├─► auth-service:8080/api/auth/validate  (on every protected request)
  │       └─► notification-service:8082/api/notifications/send  (on transfer success)
  │
  └─► notification-service (port 8082)  ◄─── internal only
```

---

## 📌 Quick Reference Table

| Method | URL                                        | Auth Required | Description                    
| POST   | `/api/auth/register`                       | ❌            | Register a new user            
| POST   | `/api/auth/login`                          | ❌            | Login and get JWT token        
| GET    | `/api/auth/validate`                       | ✅ Bearer     | Validate JWT (internal use)    
| POST   | `/api/wallet`                              | ✅ Bearer     | Create wallet for current user 
| GET    | `/api/wallet`                              | ✅ Bearer     | Get wallet balance/details     
| POST   | `/api/transaction/transfer`                | ✅ Bearer     | Transfer funds to another wallet 
| GET    | `/api/transaction/monthly`                 | ✅ Bearer     | Monthly transaction history    
| POST   | `/api/notifications/send`                  | ❌ (internal) | Send notification (internal)   
