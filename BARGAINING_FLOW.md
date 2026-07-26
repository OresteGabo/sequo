# Bargaining Flow

This document defines Sequo's customer-seller price negotiation system, also known as Negociation or `Propose ton prix`.

## Purpose

Bargaining lets a customer propose a lower price on eligible products while keeping seller control, preventing endless negotiation, and preserving clear payment/order rules.

Example: a product listed at 100,000 CFA may allow a customer to propose 80,000 CFA. The seller can accept, reject, or counter-propose if the product allows bargaining.

## Eligibility

Rules:

- Sellers can deactivate bargaining for specific products.
- Bargaining availability is product-specific and may also be variant-specific.
- Food items should usually avoid bargaining unless the seller explicitly enables it.
- A customer must be signed in before making a proposal.
- Bargaining must happen before checkout payment.
- A locked or accepted price must be included in the order pricing snapshot.

## Attempt Limits

Each party has a maximum of 3 attempts per bargaining thread.

| Party | Counts as an attempt |
| --- | --- |
| Customer | New price proposal |
| Seller | Counter-proposal |

Seller accept, seller reject without counter, and customer accept do not consume a new price-attempt unless the action includes a new price.

When either party reaches 3 attempts, they cannot submit another price in that thread.

## Price Lock Rule

Once an acceptable minimum price is set or agreed, it is locked for 24 hours.

Meaning:

- The accepted negotiated price is available to that customer for that product or variant for 24 hours.
- The seller cannot increase that locked price for that customer during the lock window.
- The customer must pay before the lock expires.
- After expiry, checkout must use the current listed price or require a new bargaining thread.
- The order snapshot stores the agreed price and lock reference.

## Historical Minimum Accepted Prices

The customer product page should display historical minimum accepted prices to inform customer proposals.

Rules:

- Show historical minimum accepted price only when backend data exists.
- Do not reveal customer identity.
- Do not reveal seller private notes or margins.
- Make the history informational, not a guaranteed offer.
- If the seller changes bargaining policy, historical values may remain visible but new bargaining can be disabled.

Suggested customer text:

```text
Lowest accepted recently: 80,000 CFA
```

Avoid fake ranges or invented minimums. If there is no accepted-price history, say no accepted-price history is available.

## State Machine

| State | Description | Next states |
| --- | --- | --- |
| `NotAvailable` | Seller disabled bargaining or product is ineligible | None |
| `Open` | Customer can make an initial proposal | `CustomerProposed`, `Expired`, `Cancelled` |
| `CustomerProposed` | Seller must accept, reject, or counter | `Accepted`, `SellerCountered`, `Rejected`, `Expired` |
| `SellerCountered` | Customer must accept, reject, or propose again if attempts remain | `Accepted`, `CustomerProposed`, `Rejected`, `Expired` |
| `Accepted` | Price is agreed and locked for 24 hours | `Paid`, `Expired` |
| `Paid` | Customer paid using accepted locked price | None |
| `Rejected` | Bargaining ended without agreement | None |
| `Expired` | 24-hour lock or thread window expired | None |
| `Cancelled` | Customer or system cancelled before agreement | None |

## Customer Flow

1. Customer opens an eligible product.
2. Product page shows listed price, bargaining availability, attempts remaining, and historical minimum accepted price if available.
3. Customer taps propose price.
4. Customer enters a CFA amount.
5. App validates that the proposal is lower than or equal to the listed price and meets backend minimum input rules.
6. Backend creates or updates the bargaining thread.
7. Seller accepts, rejects, or counters.
8. If accepted, the accepted price is locked for 24 hours.
9. Customer checks out and pays through Yas Togo or Moov Africa before the lock expires.
10. Order snapshot records the negotiated price.

## Validation Rules

- Proposals must be positive whole-CFA amounts.
- Proposals must be tied to a product or variant.
- Proposals must be lower than or equal to the current customer-visible list price unless backend policy allows another use case.
- A customer cannot bypass attempt limits by opening duplicate active threads for the same product/variant and seller.
- The backend must reject proposals after lock expiry, product deactivation, seller suspension, or stock unavailability.
- Accepted prices cannot be applied to a different product, seller, customer, or variant.

## UI Requirements

Product detail must show:

- Whether bargaining is available.
- Listed price.
- Historical minimum accepted price when available.
- Attempts remaining for the customer.
- Current seller response if a thread is active.
- Lock expiry countdown after acceptance.
- Checkout call to action using the accepted price.

Do not show bargaining controls when the seller has disabled bargaining for the product.

## Notifications

Customers should receive notifications for:

- Seller accepted proposal.
- Seller countered.
- Seller rejected.
- Price lock expiring soon.
- Price lock expired.

Sellers should receive notifications for:

- New customer proposal.
- Customer accepted seller counter.
- Customer rejected seller counter.
- Locked price paid.

## Audit And Analytics

Store:

- Product ID and variant ID.
- Seller ID.
- Customer ID.
- Listed price at start.
- Historical minimum shown to customer.
- Every proposal/counter amount.
- Attempt counts per party.
- Accepted price.
- Lock start and expiry.
- Order ID if paid.
- Rejection, expiry, and cancellation reasons.

Useful metrics:

- Bargaining conversion rate.
- Average accepted discount.
- Products with high proposal volume and low acceptance.
- Sellers disabling bargaining after high failed negotiation volume.

## Acceptance Criteria

- Seller can enable or disable bargaining per product.
- Customer and seller each have at most 3 price attempts.
- Agreed price locks for 24 hours.
- Customer sees historical minimum accepted prices where available.
- Checkout applies only valid, unexpired, accepted bargain prices.
- Payment still follows Yas Togo or Moov Africa validation before order completion.

