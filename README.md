# BankFlow API

> **Beta / Proof of Concept** — This is the backend webserver powering the core of a mobile banking application. It is not yet in production. APIs, data models, and behaviours are subject to change.

---

## What is this?

BankFlow API is the server-side backbone of a mobile banking experience. It handles everything a user expects when they open a banking app on their phone — creating an account, checking a balance, moving money to a friend, reviewing last month's transactions. The mobile client talks to this API; this API talks to the database.

This release is a **proof of concept**: the architecture is production-oriented, the security foundations are real, but it has not been hardened, audited, or load-tested for a live environment. Think of it as the first honest version of something bigger — built to validate the idea, prove the stack, and create a solid base to grow from.

The goal was not to cut corners with a prototype mindset. It was to build the real thing at a smaller scope: proper JWT auth, database migrations, containerisation, and a meaningful test suite — so that when the time comes to scale, there is nothing embarrassing to rip out.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.4.5 |
| Language | Java 17 |
| Security | Spring Security + JWT (jjwt 0.12.6) |
| Database | PostgreSQL 16 |
| ORM | Spring Data JPA + Hibernate |
| Migrations | Liquibase |
| Build Tool | Gradle |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Testing | JUnit 5, Mockito, Testcontainers |
| Containerisation | Docker + Docker Compose |

---

## Running Locally with Docker

The entire stack — API + database — spins up with a single command. No local PostgreSQL installation needed.

**Prerequisites:** Docker and Docker Compose.

```bash
# 1. Clone the repository
git clone <repo-url>
cd banking-transaction-api

# 2. (Optional) Set your own secrets
cp .env.example .env
# Edit .env — at minimum change DB_PASSWORD and JWT_SECRET in any real deployment

# 3. Boot everything
docker-compose up --build
```

Once running:
- **API base URL:** `http://localhost:8080`
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`

```bash
# Tear down
docker-compose down

# Tear down and wipe the database volume
docker-compose down -v
```

---

## Running Tests

The unit tests run with Mockito and need no external services. The integration tests spin up a real PostgreSQL instance via Testcontainers — Docker must be running.

```bash
# All tests
./gradlew test

# Unit tests only (fast, no Docker required)
./gradlew test --tests "*.service.*"

# Integration tests only
./gradlew test --tests "*.integration.*"
```

---

## API Endpoints

### Authentication

These are the only public endpoints — no token required.

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/auth/register` | Create a new user account |
| `POST` | `/api/v1/auth/login` | Authenticate and receive a JWT token |

---

### Bank Accounts

> All endpoints require `Authorization: Bearer <token>`

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/accounts` | Open a new bank account (CHECKING or SAVINGS) |
| `GET` | `/api/v1/accounts` | List all accounts owned by the authenticated user |
| `GET` | `/api/v1/accounts/{accountId}` | Get full account details |
| `GET` | `/api/v1/accounts/{accountId}/balance` | Get the current balance |

---

### Transactions

> All endpoints require `Authorization: Bearer <token>`

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/accounts/{accountId}/deposit` | Deposit money into an account |
| `POST` | `/api/v1/accounts/{accountId}/withdraw` | Withdraw money from an account |
| `POST` | `/api/v1/transactions/transfer` | Transfer money between two accounts |
| `GET` | `/api/v1/accounts/{accountId}/transactions` | Paginated history — filter by `type`, `startDate`, `endDate` |
| `GET` | `/api/v1/transactions/{transactionId}` | Fetch a single transaction by ID |

---

## Example Requests

### Register a user
```json
POST /api/v1/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "securePass123"
}
```

### Make a deposit
```json
POST /api/v1/accounts/{accountId}/deposit
Authorization: Bearer <token>
Content-Type: application/json

{
  "amount": 500.00,
  "description": "Monthly salary"
}
```

### Transfer money
```json
POST /api/v1/transactions/transfer
Authorization: Bearer <token>
Content-Type: application/json

{
  "fromAccountId": "uuid-of-source-account",
  "toAccountId": "uuid-of-destination-account",
  "amount": 150.00,
  "description": "Rent — April"
}
```

### Filtered transaction history
```
GET /api/v1/accounts/{accountId}/transactions
  ?type=DEPOSIT
  &startDate=2025-01-01
  &endDate=2025-12-31
  &page=0
  &size=20
```

---

## Environment Variables

Never commit real secrets. Copy `.env.example` to `.env` and fill in your own values before running.

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/banking_db` | PostgreSQL JDBC URL |
| `DB_USERNAME` | `postgres` | Database username |
| `DB_PASSWORD` | `postgres` | Database password — **change this** |
| `JWT_SECRET` | *(built-in dev key)* | 256-bit Base64-encoded signing key — **change this** |
| `JWT_EXPIRATION` | `86400000` | Token lifetime in milliseconds (default: 24 hours) |
| `PORT` | `8080` | HTTP port the server listens on |

---

## What's next

This is a beta. Here is what is deliberately left out and earmarked for the next iteration:

- [ ] Refresh tokens and token revocation
- [ ] Account statement export (PDF / CSV)
- [ ] Push notification hooks for the mobile client
- [ ] Role-based admin endpoints
- [ ] Rate limiting and brute-force protection
- [ ] Audit log for all financial operations
- [ ] Currency support beyond the default

---

## Author

Built by **Andrei Sarateanu**

[LinkedIn](https://www.linkedin.com/in/your-linkedin-profile) · sarateanuandrei28@gmail.com
