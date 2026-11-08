# Payments And Referrals

This document defines customer payment validation and referral delivery-credit rules.

## Supported Customer Payment Providers

The Sequo Customer App supports:

- Yas Togo.
- Moov Africa.

Cash payment, cash withdrawal, and cash referral payouts are not supported.

## Payment Validation Rule

Payment must be validated before order completion.

Required behavior:

- Checkout creates a payment attempt linked to an order draft or pending order.
- The customer chooses Yas Togo or Moov Africa.
- The backend waits for a trusted provider confirmation, verified callback, or verified payment status.
- The order cannot be marked completed without a validated payment.
- Sellers, riders, relay partners, and support agents must see a payment-safe order state.
- Failed or expired payment attempts must not create completed orders.

Recommended payment states:

| State | Meaning |
| --- | --- |
| `PaymentPending` | Customer started provider payment |
| `PaymentValidated` | Provider validation succeeded |
| `PaymentFailed` | Provider rejected or payment expired |
| `PaymentCancelled` | Customer cancelled before validation |
| `RefundPending` | A return refund instruction may be recorded; inspect its separate provider status, which remains `NOT_SUBMITTED` until provider execution exists |
| `RefundCompleted` | Provider or wallet ledger confirms refund completion |

## Checkout UX

The customer should see:

- Provider choice: Yas Togo or Moov Africa.
- Exact amount to pay.
- Payment-pending state after handoff.
- Clear retry action when payment fails.
- Protection against duplicate payment attempts.
- Order status that explains whether Sequo is waiting for payment, seller acceptance, or delivery action.

Do not show language that implies the order is complete until the backend confirms payment and order completion conditions.

## No Cash Withdrawal

Sequo balances, referral credits, discounts, and refunds must not be represented as withdrawable cash in the customer app.

Rules:

- Referral value is delivery-service discount credit only.
- Subscription discounts are service discounts only.
- Refunds go through the approved payment/refund path, not physical cash pickup.
- Relay partners do not pay cash refunds to customers.

## Referral Program

Parrainage may grant approved delivery credit. The signed PDF’s 500 CFA is an example; award amounts, qualifying conditions and expiry require SEQUO approval before production. Default unapproved configuration grants no new credit. Test rates and grants remain isolated local fixtures.

Rules:

- The referral reward is delivery credit, not cash.
- The credit applies to future Sequo delivery service fees.
- The credit cannot be withdrawn.
- The credit cannot be transferred between users.
- The credit cannot reduce product subtotal.
- The credit cannot reduce payment provider fees unless explicitly configured by backend policy.
- Only a server-approved available balance is eligible; the app does not create referral awards or subscription activations.
- The customer sees the effective available balance through `/api/subscriptions/benefits` and the actual credit applied in the server quote.
- Pending checkout reserves credit; verified successful payment applies it once. Definitive failed or cancelled payment releases its reservation.
- Quote changes require review. Checkout retries keep the frozen snapshot and do not create another credit application.

Recommended referral ledger fields:

| Field | Purpose |
| --- | --- |
| `creditId` | Stable ledger identifier |
| `sourceReferralCode` | Referral source |
| `amountCfa` | Amount authorized by an approved grant; 500 CFA is only the PDF example |
| `remainingAmountCfa` | Unused delivery credit |
| `status` | Pending, active, applied, expired, reversed |
| `earnedAt` | When the reward was earned |
| `expiresAt` | Optional expiry date |

## Refund Interaction

Return receipt, inspection and an authorized refund instruction are separate steps; inspection does not automatically execute a refund. A recorded instruction has its own reference and `REQUESTED` status, with provider payment status `NOT_SUBMITTED`. It does not confirm money was sent or received. Legacy refund records require reconciliation rather than a replacement instruction.

See [RETURNS_POLICY.md](RETURNS_POLICY.md) for the current customer flow and pending policy decisions. Refunds are not cash pickup. Restoration or reversal of used delivery credits requires an approved server policy; this document does not invent one.

## Auditability

The backend must log:

- Payment attempt creation.
- Provider selected.
- Provider reference.
- Validation callback or status check.
- Amount validated.
- Order ID linked to the payment.
- Refund trigger.
- Referral credit earn/apply/reverse events.
- Manual support or admin override.

All payment callbacks must be authenticated and idempotent.

