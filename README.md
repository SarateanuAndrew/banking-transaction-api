# Banking Transaction API

A production-ready REST API for banking operations built with Spring Boot. Supports user authentication, bank account management, and financial transactions (deposits, withdrawals, and transfers).

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.4.5 |
| Language | Java 21 |
| Security | Spring Security + JWT (jjwt 0.12.6) |
| Database | PostgreSQL 16 |
| ORM | Spring Data JPA + Hibernate |
| Migrations | Liquibase |
| Build Tool | Gradle |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Testing | JUnit 5, Mockito, Testcontainers |
| Containerization | Docker + Docker Compose |

---

## Running Locally with Docker

**Prerequisites:** Docker and Docker Compose installed.

```bash
# 1. Clone the repository
git clone <repo-url>
cd banking-transaction-api

# 2. (Optional) Configure environment variables
cp .env.example .env
# Edit .env with your preferred values

# 3. Start the application
docker-compose up --build

# API is available at: http://localhost:8080
# Swagger UI at:       http://localhost:8080/swagger-ui.html
```

To stop:
```bash
docker-compose down
```

To stop and remove data volumes:
```bash
docker-compose down -v
```

---

## Running Tests

```bash
# Run all tests (requires Docker for Testcontainers)
./gradlew test

# Run only unit tests
./gradlew test --tests "*.service.*"

# Run only integration tests
./gradlew test --tests "*.integration.*"
```

---

## API Endpoints

### Authentication

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/auth/register` | Register a new user account |
| `POST` | `/api/v1/auth/login` | Login and receive a JWT token |

### Bank Accounts

> All endpoints require `Authorization: Bearer <token>` header.

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/accounts` | Create a new bank account |
| `GET` | `/api/v1/accounts` | List all accounts for the authenticated user |
| `GET` | `/api/v1/accounts/{accountId}` | Get account details by ID |
| `GET` | `/api/v1/accounts/{accountId}/balance` | Get current balance |

### Transactions

> All endpoints require `Authorization: Bearer <token>` header.

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/accounts/{accountId}/deposit` | Deposit money into an account |
| `POST` | `/api/v1/accounts/{accountId}/withdraw` | Withdraw money from an account |
| `POST` | `/api/v1/transactions/transfer` | Transfer money between two accounts |
| `GET` | `/api/v1/accounts/{accountId}/transactions` | Get paginated transaction history (filter by `type`, `startDate`, `endDate`) |
| `GET` | `/api/v1/transactions/{transactionId}` | Get a single transaction by ID |

---

## Example Requests

### Register
```json
POST /api/v1/auth/register
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "securePass123"
}
```

### Deposit
```json
POST /api/v1/accounts/{accountId}/deposit
Authorization: Bearer <token>
{
  "amount": 500.00,
  "description": "Salary"
}
```

### Transfer
```json
POST /api/v1/transactions/transfer
Authorization: Bearer <token>
{
  "fromAccountId": "uuid-of-source",
  "toAccountId": "uuid-of-destination",
  "amount": 150.00,
  "description": "Rent payment"
}
```

### Transaction History with Filters
```
GET /api/v1/accounts/{accountId}/transactions?type=DEPOSIT&startDate=2025-01-01&endDate=2025-12-31&page=0&size=20
```

---

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/banking_db` | PostgreSQL JDBC URL |
| `DB_USERNAME` | `postgres` | Database username |
| `DB_PASSWORD` | `postgres` | Database password |
| `JWT_SECRET` | *(base64 key)* | 256-bit Base64-encoded JWT signing key |
| `JWT_EXPIRATION` | `86400000` | JWT expiration in milliseconds (default: 24h) |
| `PORT` | `8080` | Server port |

---

## Author

**Andrei Sarateanu**
[LinkedIn](https://www.linkedin.com/in/your-linkedin-profile)
