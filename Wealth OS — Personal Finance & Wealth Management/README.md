# Wealth OS — Personal Finance & Wealth Management

Wealth OS is a full-stack personal-finance dashboard for seeing cash, investments, debts, income, and spending in one place. It provides a responsive React interface, a JWT-protected Spring Boot API, and PostgreSQL persistence through Docker.

## What it does

- Creates secure user accounts and signs users in with JSON Web Tokens (JWT).
- Tracks bank accounts, credit cards, stocks, mutual funds, fixed deposits, and loans.
- Calculates net worth, total assets, liabilities, income, expenses, and cash flow.
- Records and filters income and expense transactions by category.
- Loads realistic demo data into the current user account for exploring the UI.
- Provides an interactive dashboard, portfolio screen, transaction filters, add-holding form, add-transaction form, refresh action, and profile sign-out menu.

## Technology

| Area | Technology |
| --- | --- |
| Frontend | React, Vite, CSS |
| Backend | Java 17+, Spring Boot 3, Spring Web, Spring Security, Spring Data JPA |
| Security | BCrypt password hashing, JWT (JJWT) |
| Database | PostgreSQL 16, Hibernate/JPA |
| Local platform | Docker Compose |
| Build tools | Maven, npm |

## Architecture

```text
React + Vite (localhost:5173)
            |
            | REST + JWT bearer token
            v
Spring Boot API (localhost:8080)
            |
            v
PostgreSQL in Docker (localhost:5432)
```

The current implementation is a well-structured monolith suitable for local development and MVP delivery. The service boundaries can later be separated into user, account, transaction, portfolio, analytics, notification, and reporting services. Kafka, Redis, GraphQL, AWS, and Oracle are planned production extensions rather than active dependencies in this MVP.

## Project layout

```text
.
├── backend/src/main/java/com/wealth/platform/
│   ├── WealthApplication.java     # Spring Boot entry point
│   ├── Security.java              # JWT filter and security policy
│   ├── JwtService.java            # Token creation and validation
│   ├── ApiController.java         # REST endpoints and demo-data loader
│   ├── User.java                  # User entity
│   ├── PortfolioItem.java         # Holding/liability entity
│   ├── Transaction.java           # Income/expense entity
│   └── Repositories.java          # JPA repositories
├── backend/src/main/resources/application.yml
├── frontend/src/main.jsx          # React screens and interactions
├── frontend/src/styles.css        # Responsive dashboard design
├── docker-compose.yml             # PostgreSQL service
└── pom.xml                        # Maven dependencies and build settings
```

## Prerequisites

- Java 17 or newer (Java 21 recommended)
- Maven 3.9+
- Node.js 20+ and npm
- Docker Desktop running with Linux containers / WSL 2

## Run locally

Open three terminals in the project directory.

### 1. Start PostgreSQL

```powershell
docker compose up -d
```

The database is available on port `5432` with database/user/password `wealth` for local development.

### 2. Start the API

```powershell
mvn spring-boot:run
```

### 3. Start the dashboard

```powershell
cd frontend
npm install
npm run dev
```

On Windows systems where PowerShell blocks `npm.ps1`, use `npm.cmd install` and `npm.cmd run dev` instead.

Open the Vite URL shown in the terminal, normally `http://localhost:5173`.

## User guide

1. Open the dashboard URL and enter an email address and a password of at least eight characters.
2. New credentials create an account automatically; existing credentials sign in.
3. On a new account, choose **Load interactive demo data** to add sample accounts, investments, debt, salary, SIP, and spending records.
4. Use **Overview** for net worth, allocation, cash-flow, goals, and latest activity.
5. Use **Portfolio** to inspect holdings and select **Add holding** to record a bank balance, investment, credit card, or loan.
6. Use **Transactions** to filter transactions by category and select **Add transaction** to record income or an expense.
7. Use the refresh button in the header to reload persisted data. Select the profile button to sign out.

## REST API

All endpoints return JSON. Except for authentication endpoints, send the access token on every request:

```http
Authorization: Bearer <jwt-token>
```

| Method | Endpoint | Authentication | Description |
| --- | --- | --- | --- |
| `POST` | `/api/auth/register` | No | Registers a user and returns a JWT. |
| `POST` | `/api/auth/login` | No | Validates credentials and returns a JWT. |
| `GET` | `/api/dashboard` | Yes | Returns holdings, transactions, and calculated financial totals. |
| `POST` | `/api/portfolio` | Yes | Creates a holding or liability for the signed-in user. |
| `GET` | `/api/transactions` | Yes | Lists signed-in user's transactions; optionally filter by category. |
| `POST` | `/api/transactions` | Yes | Creates an income or expense transaction. |
| `POST` | `/api/demo/seed` | Yes | Adds the starter demo portfolio when the account has no portfolio items. |

### Authentication

`POST /api/auth/register`

```json
{ "email": "you@example.com", "password": "at-least-8-characters" }
```

Response:

```json
{ "token": "eyJhbGciOiJIUzI1NiJ9..." }
```

`POST /api/auth/login` takes the same request body. Passwords are BCrypt-hashed before being stored; password hashes are excluded from API output.

### Create a holding

`POST /api/portfolio`

```json
{
  "name": "Parag Parikh Flexi Cap",
  "type": "MUTUAL_FUND",
  "value": 428000,
  "liability": 0
}
```

Supported `type` values:

`BANK_ACCOUNT`, `CREDIT_CARD`, `STOCK`, `MUTUAL_FUND`, `FIXED_DEPOSIT`, and `LOAN`.

Use `liability` for outstanding debt or card balances. A loan can have a `value` of `0` and its outstanding amount in `liability`.

### Create a transaction

`POST /api/transactions`

```json
{
  "date": "2026-08-30",
  "direction": "EXPENSE",
  "category": "Food & dining",
  "description": "Monthly groceries",
  "amount": 3200
}
```

`direction` must be `INCOME` or `EXPENSE`.

### Filter transactions

```text
GET /api/transactions?category=Food%20%26%20dining
```

### Dashboard calculation rules

- **Assets** = sum of every portfolio item's `value`.
- **Liabilities** = sum of every portfolio item's `liability`.
- **Net worth** = assets − liabilities.
- **Income** = sum of `INCOME` transaction amounts.
- **Expenses** = sum of `EXPENSE` transaction amounts.
- **Cash flow** = income − expenses.

## Database model

Hibernate creates/updates the following PostgreSQL tables locally.

| Table | Purpose | Important columns |
| --- | --- | --- |
| `app_users` | Application identity | `id`, `email` (unique), `password_hash` |
| `portfolio_item` | Cash, investments, cards, and loans | `id`, `owner_id`, `type`, `name`, `value`, `liability` |
| `transactions` | Income and expenses | `id`, `owner_id`, `date`, `direction`, `category`, `description`, `amount` |

Both `portfolio_item.owner_id` and `transactions.owner_id` point to `app_users.id`. Every data endpoint scopes queries to the authenticated user's ID, so one user cannot retrieve another user's records through the supported API.

## UI components

| Screen/component | What it provides |
| --- | --- |
| Login / registration | Single-flow account creation and sign-in screen |
| Overview | Net worth, investment amount, monthly spending, cash flow, trend chart, allocation, and recent activity |
| Portfolio | Allocation bars, full holding list, allocation weights, and add-holding modal |
| Transactions | Spending total, category filters, transaction list, and add-transaction modal |
| Demo data action | Adds realistic starter data only for the current account |
| Refresh control | Reloads dashboard values from the API |
| Profile menu | Provides account context and sign-out action |

## Configuration and security

The local configuration is in `backend/src/main/resources/application.yml`.

For any deployment, set a strong environment-specific JWT secret rather than relying on the development default:

```powershell
$env:JWT_SECRET = "use-a-long-random-secret-with-32-or-more-characters"
```

You can also override database connectivity with `DB_URL`, `DB_USER`, `DB_PASSWORD`, and set the allowed UI origin with `FRONTEND_URL`.

## Quality checks

Build the frontend production bundle:

```powershell
cd frontend
npm run build
```

Run the backend tests/build:

```powershell
mvn test
```

## Future roadmap

- Database migrations with Flyway and DTO/service-layer validation
- Financial goals, SIP/EMI calculators, alerts, and budgets
- PDF and Excel reports
- CSV import and bank/broker data integrations
- Redis cache, Kafka events, analytics and notification services
- GraphQL read API
- AWS deployment, observability, backups, rate limiting, and MFA

## License

This project is currently unlicensed. Add a license before public or commercial distribution.
