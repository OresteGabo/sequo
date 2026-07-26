# Delivery Pricing

This document defines the customer delivery-fee rules for Sequo.

## Core Formula

Delivery pricing uses a minimum fee plus a per-kilometer extension:

```text
If distance <= 5 km:
  deliveryFee = 400 CFA

If distance > 5 km:
  deliveryFee = 400 CFA + ((billableKm - 5) * 100 CFA)
```

Where:

- `billableKm` is the route distance rounded up to the next full kilometer.
- The first 5 km are covered by the 400 CFA minimum fee.
- Each kilometer after 5 km costs 100 CFA.
- Pricing must be calculated server-side.

## Examples

| Route distance | Billable km | Delivery fee before discounts |
| --- | ---: | ---: |
| 1.2 km | 2 km | 400 CFA |
| 5.0 km | 5 km | 400 CFA |
| 5.1 km | 6 km | 500 CFA |
| 8.4 km | 9 km | 800 CFA |
| 12.0 km | 12 km | 1,100 CFA |
| 12.3 km | 13 km | 1,200 CFA |

The customer UI should avoid surprise rounding. Show the route distance and final billable fee before payment.

## Subscription Discounts

Monthly paying subscribers receive automatic percentage discounts on delivery fees. Multi-year subscribers receive deeper discounts than monthly subscribers.

Rules:

- The discount percent comes from the active subscription plan on the backend.
- Discounts apply automatically at checkout when the subscription is active.
- The customer must not need to enter a promo code for their subscription discount.
- Multi-year plans must have a larger delivery-fee discount than monthly plans.
- Subscription discounts apply to delivery service fees, not product prices.
- The invoice must show the original delivery fee, discount amount, and discounted delivery fee.

Recommended plan fields:

| Field | Purpose |
| --- | --- |
| `planId` | Stable backend identifier |
| `billingPeriod` | Monthly, annual, multi-year |
| `deliveryDiscountPercent` | Percent applied to eligible delivery fee |
| `startsAt` | Subscription start time |
| `expiresAt` | Subscription expiry time |
| `status` | Active, pending, expired, cancelled |

## Referral Delivery Credit

Referral credit is applied after the subscription delivery discount.

Order of operations:

1. Calculate base delivery fee.
2. Apply subscription delivery discount if active.
3. Apply referral delivery credit if available.
4. Charge the remaining payable amount through Yas Togo or Moov Africa.

Referral credit cannot make the delivery component negative.

## Consolidated Orders

For grouped seller orders, Sequo should calculate the customer-facing fee on the consolidated delivery, not as separate final delivery charges for each seller.

Rules:

- Customer sees one delivery fee for one consolidated package.
- Seller sub-orders keep separate accounting internally.
- If the order is split after payment due to operational constraints, Sequo must show the customer whether the delivery fee changes, stays subsidized, or requires support approval.

## Pickup And Point De Relai

Pickup or Click & Collect should not charge a rider delivery fee.

Point de Relai delivery or return handling may have additional operational fees only if configured by the backend and disclosed before payment. Do not silently add relay fees after payment.

## Display Requirements

Checkout must show:

- Route distance.
- Delivery fee before discounts.
- Subscription discount.
- Referral credit applied.
- Final delivery fee.
- Final order total.

Example:

```text
Delivery distance: 8.4 km
Delivery fee: 800 CFA
Subscriber discount: -120 CFA
Referral credit: -500 CFA
Delivery due: 180 CFA
```

## Backend Requirements

- Delivery pricing is server-authoritative.
- Price calculation must be idempotent.
- The order stores a pricing snapshot.
- Admin overrides must be audited.
- Tests must cover edge distances: 0 km, 5 km, 5.1 km, large distance, active subscription, expired subscription, referral credit larger than fee, and consolidated orders.

