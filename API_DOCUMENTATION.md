# 🌐 API Documentation: E-Wallet Transaction System

This document describes the REST API contract for the three microservices in the system. The candidate's implementation must conform exactly to these endpoint names, request bodies, response formats, and HTTP status codes.

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
  "username": "johndoe",
  "password": "SecurePassword123",
  "email": "johndoe@example.com"
}
```

**Response (`201 Created`):**
```json
{
  "status": "success",
  "message": "User registered successfully",
  "userId": 1001
}
```

**Response (`400 Bad Request` - e.g., Username/Email already exists):**
```json
{
  "status": "error",
  "message": "Username or Email already exists"
}
```

---

### 2. Login User
Authenticate and receive an access token.
- **Method:** `POST`
- **URL:** `/api/auth/login`
- **Content-Type:** `application/json`

**Request Body:**
```json
{
  "username": "johndoe",
  "password": "SecurePassword123"
}
```

**Response (`200 OK`):**
```json
{
  "status": "success",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjEwMDEs...",
  "expiresIn": 3600
}
```

**Response (`401 Unauthorized` - Invalid credentials):**
```json
{
  "status": "error",
  "message": "Invalid username or password"
}
```

---

### 3. Validate Token
Verify a token's validity and extract user details. This endpoint is called internally by other microservices.
- **Method:** `GET`
- **URL:** `/api/auth/validate`
- **Headers:** 
  - `Authorization: Bearer <token>`

**Response (`200 OK`):**
```json
{
  "valid": true,
  "userId": 1001,
  "username": "johndoe",
  "email": "johndoe@example.com",
  "role": "USER"
}
```

**Response (`401 Unauthorized` - Token expired or invalid):**
```json
{
  "valid": false,
  "message": "JWT Token has expired or is invalid"
}
```

---

## 💼 Wallet & Payment Service (`wallet`)
- **Default Port:** `8081`
- **Base Path:** `/api`
- **Security:** Most endpoints require the `Authorization: Bearer <token>` header, which the service will validate by calling `auth-service`'s validate endpoint.

### 1. Create Wallet
Create a wallet for the authenticated user.
- **Method:** `POST`
- **URL:** `/api/wallets`
- **Headers:** 
  - `Authorization: Bearer <token>`
- **Request Body (Optional - defaults to IDR):**
```json
{
  "currency": "IDR"
}
```

**Response (`201 Created`):**
```json
{
  "walletId": 501,
  "userId": 1001,
  "balance": 0.00,
  "currency": "IDR",
  "createdAt": "2026-06-05T05:54:00Z"
}
```

---

### 2. Get Wallet Balance
Fetch the wallet details and balance of the authenticated user.
- **Method:** `GET`
- **URL:** `/api/wallets/balance`
- **Headers:** 
  - `Authorization: Bearer <token>`

**Response (`200 OK`):**
```json
{
  "walletId": 501,
  "balance": 150000.00,
  "currency": "IDR"
}
```

---

### 3. Transfer Funds (Payment Transaction)
Initiate a money transfer to another wallet. This operation **must** use pessimistic locking to prevent race conditions.
- **Method:** `POST`
- **URL:** `/api/transactions/transfer`
- **Headers:** 
  - `Authorization: Bearer <token>`
- **Content-Type:** `application/json`

**Request Body:**
```json
{
  "recipientWalletId": 502,
  "amount": 25000.00,
  "description": "Payment for lunch"
}
```

**Response (`200 OK`):**
```json
{
  "status": "SUCCESS",
  "referenceNumber": "TX-20260605-A89B4C",
  "amount": 25000.00,
  "senderWalletId": 501,
  "recipientWalletId": 502,
  "description": "Payment for lunch",
  "timestamp": "2026-06-05T06:01:23Z"
}
```

**Response (`400 Bad Request` - e.g., Insufficient funds or transferring to own wallet):**
```json
{
  "status": "FAILED",
  "message": "Insufficient balance for this transaction"
}
```

**Response (`404 Not Found` - Recipient wallet does not exist):**
```json
{
  "status": "FAILED",
  "message": "Recipient wallet not found"
}
```

---

### 4. Get Transaction History
Retrieve the wallet's transaction history. The list must be processed and filtered in-memory using **Java Streams** before responding.
- **Method:** `GET`
- **URL:** `/api/transactions/history`
- **Headers:** 
  - `Authorization: Bearer <token>`
- **Query Parameters (Optional):**
  - `status` (e.g. `SUCCESS`, `FAILED`)
  - `minAmount` (e.g. `10000.00`)

**Response (`200 OK`):**
```json
{
  "walletId": 501,
  "totalVolume": 25000.00,
  "currency": "IDR",
  "transactions": [
    {
      "id": 8001,
      "type": "TRANSFER_OUT",
      "amount": 25000.00,
      "counterpartyWalletId": 502,
      "referenceNumber": "TX-20260605-A89B4C",
      "status": "SUCCESS",
      "description": "Payment for lunch",
      "date": "2026-06-05"
    }
  ]
}
```

---

## 🔔 Notification Service (`notification`)
- **Default Port:** `8082`
- **Base Path:** `/api/notifications`

### 1. Send Notification
Endpoint invoked internally by the `wallet` service.
- **Method:** `POST`
- **URL:** `/api/notifications/send`
- **Content-Type:** `application/json`

**Request Body:**
```json
{
  "userId": 1002,
  "title": "Funds Received",
  "message": "You have received IDR 25,000.00 from John Doe (Wallet: 501).",
  "notificationType": "EMAIL"
}
```

**Response (`200 OK`):**
```json
{
  "status": "sent",
  "notificationId": 9001
}
```
