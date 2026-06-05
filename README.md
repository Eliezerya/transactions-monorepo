# 💳 Mid-Level Java Backend Developer Coding Test: E-Wallet Transaction System

Welcome to the **SOD Tech E-Wallet Transaction System** recruitment coding test. This test is designed to evaluate your experience as a mid-level Java/Spring Boot developer. You are expected to implement a secure, containerized, and decoupled microservices system that handles payment transactions.

---

## 📂 Project Structure

This repository is organized as a multi-module microservice architecture containing three main Spring Boot projects:

```text
.
├── auth-service/           # User Registration, Authentication & Token Validation
├── wallet/                 # Wallet Management & Payment/Transfer Transactions (Payment Service)
├── notification/           # Simulated User Notifications (Email, SMS, Push)
├── SPECIFICATION.md        # Detailed functional & technical requirements
├── API_DOCUMENTATION.md    # API contracts (endpoints, requests, responses, status codes)
└── README.md               # This file (getting started instructions)
```

---

## 🛠️ Required Technologies & Implementation Constraints

To pass this evaluation, your implementation **must** cover the following pillars without overengineering:

1. **Spring IoC (Inversion of Control)**: Proper decoupling of layers using Dependency Injection, custom bean definitions where appropriate, and correct stereotype annotations (`@Service`, `@Repository`, `@RestController`).
2. **Java Stream API**: Process data sets in memory (such as transaction history, spend aggregation, and user summaries) cleanly using Java 8+ streams (e.g., `.filter()`, `.map()`, `.collect()`, `.reduce()`).
3. **Intermediate Native SQL Query**: Maintain transaction safety and concurrency safety. You must handle race conditions during wallet transfers using pessimistic locking (`SELECT ... FOR UPDATE` via `@Query(value = "...", nativeQuery = true)`) and retrieve complex reports using native SQL joins.
4. **Containerization & Microservices**: 
   - Write optimized `Dockerfile`s for each service.
   - Configure a root-level `docker-compose.yml` to orchestrate the entire stack.
   - **Database-per-Service**: `auth-service` and `wallet` **must** run on separate, isolated database instances (e.g., PostgreSQL for auth, and a separate PostgreSQL/MySQL instance for wallet).

---

## 📖 Getting Started

To begin the coding test:

1. Read the complete **[Functional & Technical Specification (SPECIFICATION.md)](./SPECIFICATION.md)** to understand the requirements, schema design, and expected business logic.
2. Read the **[API Documentation (API_DOCUMENTATION.md)](./API_DOCUMENTATION.md)** to align your controller endpoints with the required payload structures and status codes.
3. Configure your local Docker environment.
4. Implement the requirements in each service folder.

Good luck! We look forward to reviewing your clean, performant, and well-structured code.
