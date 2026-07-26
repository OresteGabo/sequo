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
| `RefundPending` | Refund has been triggered but provider has not confirmed it |
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

Parrainage gives 500 CFA worth of delivery-service discount.

Rules:

- The referral reward is delivery credit, not cash.
- The credit applies to future Sequo delivery service fees.
- The credit cannot be withdrawn.
- The credit cannot be transferred between users.
- The credit cannot reduce product subtotal.
- The credit cannot reduce payment provider fees unless explicitly configured by backend policy.
- The credit is applied automatically at checkout when eligible.

Recommended referral ledger fields:

| Field | Purpose |
| --- | --- |
| `creditId` | Stable ledger identifier |
| `sourceReferralCode` | Referral source |
| `amountCfa` | Always 500 CFA per earned referral credit unless policy changes |
| `remainingAmountCfa` | Unused delivery credit |
| `status` | Pending, active, applied, expired, reversed |
| `earnedAt` | When the reward was earned |
| `expiresAt` | Optional expiry date |

## Refund Interaction

When a return is inspected and accepted by Sequo:

- Refund trigger is automatic.
- Customer should see refund status.
- Refund must not be issued as cash from a Point de Relai.
- Delivery credits used on the order should follow backend refund policy and be visible in the ledger.

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

