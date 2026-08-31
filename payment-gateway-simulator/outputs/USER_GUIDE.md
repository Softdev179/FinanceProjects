# PayFlow User Guide

This guide explains how to operate the PayFlow Payment Gateway Simulator from the merchant dashboard and Swagger API interface.

## 1. Start the application

### Start the backend

Open Command Prompt and run:

```cmd
cd /d "C:\Users\Akshay G Rao\Documents\Codex\2026-08-31\payment-gateway-simulator-this-can-become\backend"
mvn spring-boot:run
```

Wait until the console reports that the application started on port `8080`.

### Start the frontend

Open a second Command Prompt window:

```cmd
cd /d "C:\Users\Akshay G Rao\Documents\Codex\2026-08-31\payment-gateway-simulator-this-can-become\frontend"
npm install
npm run dev
```

Open http://localhost:5173. API documentation is available at http://localhost:8080/swagger-ui.html.

## 2. Sign in

The application creates a demonstration merchant account on the first backend startup:

```text
Email: demo@payflow.local
Password: Demo@12345
Merchant: merchant_demo
```

1. Open the dashboard.
2. Enter the email and password.
3. Select **Sign in securely**.
4. The backend validates the BCrypt password and returns a signed JWT.
5. The browser attaches that JWT to protected API requests.

Click the profile initials in the upper-right corner to sign out.

### Register another merchant

Registration is currently available through Swagger:

1. Open Swagger UI.
2. Expand `POST /api/v1/auth/register`.
3. Select **Try it out**.
4. Enter:

```json
{
  "email": "owner@example.com",
  "password": "SecurePassword123!",
  "businessName": "Example Store"
}
```

5. Select **Execute**.
6. Copy the returned JWT or use the dashboard login page with the new credentials.

Passwords must contain at least eight characters. Use a stronger production password than the example.

## 3. Understand the dashboard

The Overview screen reads live database records through the analytics API.

- **Total payment volume**: sum of captured payments before refunds.
- **Successful payments**: captured, partially refunded, and refunded payments.
- **Success rate**: successful payments divided by the merchant's payment count.
- **Pending settlement**: estimated captured value after refunds and the simulated 2% fee.
- **Total refunds**: amount returned to customers.
- **Payment volume chart**: payment amounts created during the last seven days.
- **Payment methods chart**: number of card, UPI, net-banking, and wallet payments.
- **Recent payments**: latest payment records for the signed-in merchant.

Refreshing the page reloads information from the backend. Dashboard totals are not stored as hardcoded frontend values.

## 4. Create a payment

1. Select **Create payment** in the upper-right corner.
2. Enter an amount greater than or equal to `1.00`.
3. Select the currency and payment method.
4. Enter the customer's email address.
5. Add a description such as an order number.
6. Select **Create & authorize**.

The dashboard generates a unique `Idempotency-Key`, creates the payment, and authorizes it using the demonstration success override. The payment is now `AUTHORIZED`; it is not captured yet.

### Why idempotency matters

Every creation request needs an `Idempotency-Key`. Repeating the same request with the same key returns the original payment instead of charging twice. Using the same key with different merchant, amount, or currency data returns `409 Conflict`.

## 5. Understand payment statuses

| Status | Meaning | Next action |
|---|---|---|
| `CREATED` | Payment intent exists but the bank has not approved it | Authorize |
| `AUTHORIZED` | Bank approved and reserved the amount | Capture |
| `CAPTURED` | Charge completed and is eligible for settlement or refund | Refund or settle |
| `FAILED` | Bank simulator declined authorization | Retry |
| `PARTIALLY_REFUNDED` | Part of the captured amount was returned | Refund the remaining balance if required |
| `REFUNDED` | Entire captured amount was returned | No additional refund allowed |

Invalid transitions return `409 Conflict`. For example, a `CREATED` payment cannot be captured before authorization.

## 6. Authorize a payment

Authorization asks the simulated bank to approve or decline a payment.

Payments created from the dashboard are authorized automatically. To test the normal bank simulator through Swagger:

1. Create a payment with `POST /api/v1/payments`.
2. Copy its `pay_...` ID.
3. Call `POST /api/v1/payments/{id}/authorize`.
4. Use `{"forceSuccess": false}` for the simulated 82% approval rate.
5. Use `{"forceSuccess": true}` for deterministic approval.

Successful authorization changes the status to `AUTHORIZED`. A decline changes it to `FAILED` and records a failure reason.

## 7. Capture an authorized payment

Capture completes the charge after authorization.

1. Open **Payments** in the sidebar.
2. Find an `AUTHORIZED` payment.
3. Select **Capture** in its Action column.
4. Confirm that the status becomes `CAPTURED`.

The captured payment now contributes to payment volume and is eligible for refunds and settlements.

## 8. Retry a failed payment

1. Open **Payments**.
2. Find a payment with `FAILED` status.
3. Select **Retry**.
4. The bank simulator performs authorization again.
5. If approved, capture it using the **Capture** action.

Every authorization and retry increments `attemptCount`. Retrying does not create another payment record.

## 9. Refund a payment

A refund returns part or all of a captured payment to the customer. Refund creation is currently performed through Swagger/API; the dashboard Refunds screen displays the resulting history.

1. Open Swagger UI.
2. Log in using `POST /api/v1/auth/login`.
3. Copy the returned token.
4. Select **Authorize** in Swagger and enter `Bearer <token>` if the Swagger authorization dialog is configured, or send the header from an API client.
5. Expand `POST /api/v1/payments/{id}/refunds`.
6. Enter a captured payment ID.
7. Enter the refund data:

```json
{
  "amount": 250.00,
  "reason": "Customer returned the item"
}
```

8. Execute the request.
9. Open **Refunds** in the dashboard to see the refund ID, payment ID, amount, reason, and time.

The refund cannot exceed the remaining refundable amount. A partial amount produces `PARTIALLY_REFUNDED`; refunding the full remaining amount produces `REFUNDED`.

## 10. Generate a settlement

A settlement represents the payout from PayFlow to the merchant:

```text
Eligible captured value − refunds − simulated 2% fee = net payout
```

1. Open **Settlements**.
2. Review existing settlement records.
3. Select **Generate settlement**.
4. A new settlement snapshot is created.
5. Review its gross amount, processing fee, net payout, status, and time.

This simulator creates a snapshot using current eligible payments. A production system would track which ledger entries were already settled and enforce payout cycles and cutoff times.

## 11. Run reconciliation

Reconciliation compares gateway records with the simulated processor total.

1. Open **Reconciliation**.
2. Select **Run reconciliation**.
3. A notification displays the number of records checked and the result.

`BALANCED` means gateway and processor totals agree. The response also contains records scanned, records matched, mismatch count, gateway total, processor total, and run time.

## 12. Configure webhooks

Webhooks notify a merchant server when payment events happen. Endpoint management currently uses the API.

1. Prepare a public HTTP or HTTPS endpoint that accepts `POST` requests.
2. Register it using `POST /api/v1/webhooks/endpoints`:

```json
{
  "merchantId": "merchant_demo",
  "url": "https://example.com/payflow/webhook"
}
```

3. Create, authorize, capture, fail, or refund payments.
4. Inspect recent deliveries with `GET /api/v1/webhooks/events`.

Supported events include:

- `payment.authorized`
- `payment.failed`
- `payment.captured`
- `refund.created`

PayFlow sends `X-PayFlow-Signature`, a hexadecimal HMAC-SHA256 signature of the raw JSON body. Merchant servers should calculate the same signature with `WEBHOOK_SECRET` and compare it before trusting an event.

## 13. Use notifications

1. Select the bell icon in the header.
2. The activity panel displays recent captured, failed, partially refunded, and refunded payments.
3. A red indicator appears when failed payments exist.
4. Select an activity item or the close icon to close the panel.

These are dashboard activity notifications derived from recent payments. They are separate from outbound merchant webhooks.

## 14. Use PostgreSQL

The application uses file-backed H2 by default. To use PostgreSQL, create the database and set:

```cmd
set DB_URL=jdbc:postgresql://localhost:5432/payflow
set DB_USER=payflow
set DB_PASSWORD=your_password
set DB_DRIVER=org.postgresql.Driver
set JWT_SECRET=replace-with-a-long-random-production-secret
mvn spring-boot:run
```

Alternatively, from the project root run:

```cmd
docker compose up --build
```

The important PostgreSQL tables are:

- `merchant_users`
- `payments`
- `refunds`
- `settlements`
- `webhook_endpoints`
- `webhook_events`

## 15. Common problems

### Maven says there is no POM

Use `/d` when switching drives in Command Prompt:

```cmd
cd /d "C:\Users\Akshay G Rao\Documents\Codex\2026-08-31\payment-gateway-simulator-this-can-become\backend"
```

### The dashboard shows a network error

Confirm the backend is running on port `8080` and the frontend uses `VITE_API_URL=http://localhost:8080/api/v1`.

### An API returns `401 Unauthorized`

Log in again and send the current JWT as `Authorization: Bearer <token>`. Tokens expire after 12 hours by default.

### An API returns `403 Forbidden`

The token is valid but does not have permission for the requested operation.

### An action returns `409 Conflict`

The payment is in an invalid state for that action, or an idempotency key was reused with different request data.

### A refund returns `400 Bad Request`

Confirm the amount is positive, the payment has been captured, and the requested refund does not exceed the remaining refundable balance.

### Demo records do not reappear

Demo payments are inserted only when the payments table is empty. Existing database data is retained across restarts.

## 16. Suggested demonstration sequence

For an interview or portfolio walkthrough:

1. Sign in with the demo merchant.
2. Explain the JWT merchant claim and protected API boundary.
3. Create a payment from the dashboard.
4. Show that repeating an idempotent API request does not duplicate the payment.
5. Capture the authorized payment.
6. Create a partial refund through Swagger and show it on the Refunds page.
7. Generate a settlement and explain gross, fee, and net payout.
8. Run reconciliation.
9. Open the notification bell.
10. Explain webhook HMAC signatures and production outbox/retry improvements.
