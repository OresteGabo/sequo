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

An approved plan and a verified eligible subscription period may enable automatic delivery discounts. Raw `ACTIVE` status alone does not establish eligibility.

Rules:

- Rates, caps, eligibility periods and approval references are server-owned; production values and conditions require SEQUO approval.
- Loyalty is optional and requires an approved rule plus verified tenure. The PDF does not prescribe annual or multi-year tiers, thresholds or rates.
- Without approved eligibility, the server applies no subscription or loyalty discount. Local fixture rates do not activate production benefits.
- Subscription and loyalty discounts affect delivery charges, not product prices.
- The customer reads `/api/subscriptions/benefits` and refreshes for effective status and available delivery credit. Failed or unknown reads do not retain active benefit claims.
- The app does not add subscription purchase, payment-provider activation or referral-grant controls in this flow.

Recommended plan fields:

| Field | Purpose |
| --- | --- |
| `planId` | Stable backend identifier |
| `deliveryDiscountPercent` | Approved percent applied to eligible delivery charges |
| `monthlyDiscountCapCfa` | Optional approved monthly cap |
| `approvalReference` | Plan approval evidence |
| `currentPeriodStartsAt` / `currentPeriodEndsAt` | Verified period boundaries |
| `activationReference` | Subscription activation evidence |
| `status` | Stored state; effective eligibility is checked separately |

## Referral Delivery Credit

The server applies approved subscription and optional loyalty benefits, then referral credit to the remaining delivery charge. Credit cannot reduce the product subtotal or make delivery negative.

The signed PDF gives 500 CFA as an example. Amounts, award conditions and expiry require SEQUO approval; no 500 CFA production default is inferred.
Pending checkout reserves the selected credit and benefit capacity. Verified successful payment applies the reservation once; a definitive failed or cancelled checkout releases it. Replays use the frozen checkout snapshot rather than spending credit again.

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
- Subscription discount and any approved loyalty discount.
- Referral credit actually applied.
- Final delivery fee.
- Final order total.

Local test fixture example only; these benefit rates and credit amounts are not approved production values:

```text
Delivery distance: 8.4 km
Delivery before benefits: 800 CFA
Subscription discount: -56 CFA
Loyalty discount: -24 CFA
Referral credit applied: -180 CFA
Delivery due: 540 CFA
```

Checkout reads `deliveryBaseFeeCfa`, `subscriptionDiscountCfa`, `loyaltyDiscountCfa`, `referralCreditAppliedCfa` and the final amounts from the server. It submits the reviewed quote revision, never client-selected discount rates, credit amounts or totals. Legacy quotes without the additive benefit fields retain their original final delivery amount.

## Backend Requirements

- Delivery pricing is server-authoritative.
- Price calculation must be idempotent.
- The order stores a pricing snapshot.
- Admin overrides must be audited.
- Tests must cover edge distances: 0 km, 5 km, 5.1 km, large distance, active subscription, expired subscription, referral credit larger than fee, and consolidated orders.

