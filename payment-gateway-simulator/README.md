# PayFlow — Payment Gateway Simulator

For a step-by-step walkthrough of every workflow, see [the user guide](outputs/USER_GUIDE.md).

A portfolio-grade mini Razorpay/Stripe-style payment platform built with **Spring Boot 3, Java 20+, React, Vite, JPA, H2/PostgreSQL**, and a responsive merchant dashboard.

## What is implemented

- Payment creation with database-enforced idempotency
- Simulated bank authorization (82% success, or deterministic success for demos)
- Explicit capture and payment state validation
- Full and partial refunds
- Failed-payment retries with attempt tracking
- Signed webhook delivery with delivery logs
- Merchant settlements with a simulated 2% processing fee
- Reconciliation report comparing gateway and processor totals
- Dashboard analytics, transaction history, filters, and payment actions
- JWT authentication, BCrypt password hashing, stateless sessions, and merchant-scoped payment access
- OpenAPI/Swagger documentation, validation, consistent API errors, tests, Docker setup, and seeded demo data

## Architecture and payment flow

```text
React Merchant Dashboard
          │ REST / JSON
          ▼
Spring Boot Payment Gateway
  ├── Idempotency guard (unique DB key)
  ├── Payment state machine
  ├── Simulated payment processor / bank decision
  ├── Refunds, retries and settlements
  ├── HMAC-SHA256 webhook dispatcher
  └── Reconciliation and analytics
          │
          ▼
     H2 or PostgreSQL
```

Valid lifecycle:

```text
CREATED ──authorize──> AUTHORIZED ──capture──> CAPTURED
   │                                              │
   └──authorize declined──> FAILED ──retry──┐     ├──partial refund──> PARTIALLY_REFUNDED
                                           └─────┘──full refund─────> REFUNDED
```

## Run locally

Requirements: Java 20+, Maven 3.9+, Node 20+.

Terminal 1:

```bash
cd backend
mvn spring-boot:run
```

Terminal 2:

```bash
cd frontend
npm install
npm run dev
```

Open:

- Dashboard: http://localhost:5173
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 console: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:file:./data/payflow`, user `sa`)

The application seeds 18 demo payments on its first run. To use PostgreSQL and run the full stack instead, execute `docker compose up --build`.

## API reference

All requests and responses use JSON. Except for registration and login, APIs require `Authorization: Bearer <JWT>`. Amounts are decimal major currency units (for example, `499.00` INR). The interactive, always-current API contract is available in Swagger.

### Authentication

- `POST /api/v1/auth/register` with `{"email":"owner@example.com","password":"at-least-8-characters","businessName":"Acme"}`
- `POST /api/v1/auth/login` with `{"email":"demo@payflow.local","password":"Demo@12345"}`

Both return a signed JWT containing the merchant ID, role, business name, subject, issue time, and expiry. Send it as `Authorization: Bearer <token>`. The seeded account is `demo@payflow.local` / `Demo@12345`. Set a strong `JWT_SECRET` outside source control in production.

### Create a payment

`POST /api/v1/payments`

Required header: `Idempotency-Key`. Reusing the key with the same merchant, amount, and currency returns the original payment. Reusing it with different values returns `409 Conflict`. A unique database constraint also prevents duplicates during concurrent requests.

```bash
curl -X POST http://localhost:8080/api/v1/payments \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: order-1042-attempt-1" \
  -d '{
    "merchantId":"merchant_demo",
    "amount":2499.00,
    "currency":"INR",
    "method":"upi",
    "description":"Order #1042",
    "customerEmail":"buyer@example.com"
  }'
```

Response: `201 Created` with a `pay_...` ID and `CREATED` status.

### Get or list payments

- `GET /api/v1/payments/{paymentId}` — fetch one payment
- `GET /api/v1/payments?merchantId=merchant_demo&page=0&size=20` — paginated payment list; merchant filter is optional

### Authorize a payment

`POST /api/v1/payments/{paymentId}/authorize`

```json
{"forceSuccess": false}
```

The bank simulator normally has an 82% approval rate. Use `forceSuccess: true` for a deterministic demo. Valid from `CREATED`; a failed payment can also be passed through authorization by the retry endpoint.

### Capture an authorized payment

`POST /api/v1/payments/{paymentId}/capture`

No request body. Only `AUTHORIZED` payments can be captured. Invalid state transitions return `409 Conflict`.

### Retry a failed payment

`POST /api/v1/payments/{paymentId}/retry`

Runs the bank simulation again and increments `attemptCount`. Only `FAILED` payments can be retried.

### Refund a captured payment

`POST /api/v1/payments/{paymentId}/refunds`

```json
{"amount":499.00,"reason":"Customer returned the item"}
```

The sum of refunds cannot exceed the captured amount. A partial refund produces `PARTIALLY_REFUNDED`; refunding the remaining balance produces `REFUNDED`.

### Register a webhook endpoint

`POST /api/v1/webhooks/endpoints`

```json
{"merchantId":"merchant_demo","url":"https://example.com/payflow/webhook"}
```

Events are emitted for `payment.authorized`, `payment.failed`, `payment.captured`, and `refund.created`. Every request includes `X-PayFlow-Signature`, a hex HMAC-SHA256 of the raw request body using `WEBHOOK_SECRET` (default `whsec_demo_payflow`). Delivery history is at `GET /api/v1/webhooks/events`.

### Generate and list settlements

- `POST /api/v1/settlements` with `{"merchantId":"merchant_demo"}` — generates a snapshot settlement using eligible captured value minus refunds and a 2% fee
- `GET /api/v1/settlements?merchantId=merchant_demo` — lists settlements newest first

### Reconcile records

`POST /api/v1/reconciliation/run` with `{}`. Returns records scanned/matched, totals, mismatch count, and `BALANCED` status. The simulator uses the internal processor ledger, so normal runs have no mismatch.

### Dashboard analytics

`GET /api/v1/analytics/dashboard` returns total volume, success/failure counts, refund total, success rate, pending settlement value, seven-day trend, and payment-method breakdown.

## Idempotency design

The client-provided key is stored with the payment under a unique database constraint. The service checks for an existing record before creation and also catches a constraint violation, which closes the race where two identical concurrent requests both pass the initial lookup. Payload identity is checked before replaying the existing result.

Production improvements would scope keys to the authenticated merchant, persist a canonical request hash and serialized response in a dedicated idempotency table, lock or insert atomically, expire keys after a documented retention period, and never rely on an in-memory map.

## Configuration

| Variable | Default | Purpose |
|---|---|---|
| `PORT` | `8080` | Backend HTTP port |
| `DB_URL` | file-backed H2 URL | JDBC URL |
| `DB_USER` | `sa` | Database user |
| `DB_PASSWORD` | empty | Database password |
| `DB_DRIVER` | `org.h2.Driver` | JDBC driver |
| `CORS_ORIGIN` | `http://localhost:5173` | Allowed dashboard origin |
| `WEBHOOK_SECRET` | demo secret | HMAC signing secret |
| `VITE_API_URL` | `http://localhost:8080/api/v1` | Frontend API base URL |

## Tests and production considerations

Run backend tests with `cd backend && mvn test`; build the frontend with `cd frontend && npm run build`.

This is a simulator, not a PCI-compliant payment product. It deliberately stores no card numbers or credentials. A real deployment should add authentication/API keys, per-merchant authorization, encryption and secrets management, a durable queue/outbox for webhook retries, settlement cutoffs and ledger entries, rate limiting, observability, fraud controls, database migrations, and processor-specific reconciliation files.
