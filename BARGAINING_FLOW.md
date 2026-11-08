# Bargaining MVP flow

This records the Task 18 implementation. The signed SEQUO MVP PDF defines scope.
Bargaining applies to a product and its authorized customer/merchant pair.
The MVP does not introduce variant-specific agreements.

## Eligibility and privacy

- The customer signs in; the server resolves the product's merchant and checks ownership on every action.
- The merchant enables bargaining per product. The product and merchant must be active, with a valid catalog price.
- A proposal is a positive whole-CFA amount strictly below the current catalog base price.
- The server enforces a configured merchant floor; public catalog/customer responses hide that private floor.
- Historical minimum accepted prices are not displayed. The additional policy contemplated by contract point 55 requires SEQUO approval.

## Attempts and replies

- Defaults are three customer price proposals and three merchant counters.
- Counts cover the same customer, merchant and product across threads; opening another thread does not reset them.
- Accept/reject decisions and messages do not consume price attempts. There is no automatic cooldown or attempt reset.
- The server supplies counts, configured limits and `nextActor`; clients refresh instead of decrementing counters locally.
- Accept, reject and merchant counter actions reference the latest pending opposite offer using `expectedOfferId`.
- A stale response is rejected. A decision uses the server's pending amount, without a client-selected acceptance price.
- Stable `idempotencyKey` values permit retries of an identical action; a conflicting reuse is rejected.

## Authenticated API

| Request | Purpose |
| --- | --- |
| `GET /api/bargaining/threads` | Customer's persisted threads |
| `GET /api/bargaining/threads?merchantId=…` | Authorized merchant's inbox |
| `GET /api/bargaining/threads/{id}` | Scoped thread readback |
| `POST /api/bargaining/threads` | Initial customer proposal |
| `POST /api/bargaining/threads/{id}/offers` | Unified offer, counter, decision or message |

Create sends `productId`, `amountCfa`, optional `message` and a stable `idempotencyKey`.
The offers body uses `offerType` (`OFFER`, `COUNTER_OFFER`, `ACCEPT`, `REJECT`, `MESSAGE`),
optional `amountCfa`/`message`, `expectedOfferId` where required, and `idempotencyKey`.
Neither body grants merchant scope or lets a client mark payment successful.

The thread DTO retains IDs, `status`, `lastOfferCfa`, `acceptedPriceCfa`, `openedAt` and `offers`.
It also supplies `productName`, `catalogPriceCfa`, `bargainingEnabled`, `customerAttemptsUsed`,
`merchantAttemptsUsed`, `maxCustomerAttempts`, `maxMerchantAttempts`, `acceptedAt`, `expiresAt`
and `nextActor`. Offer history includes the actor, action, amount, message and creation time.

## Accepted price and basket

- Acceptance creates a server-owned price agreement, with a default 24-hour validity window.
- The negotiated amount replaces the product's base unit price. Selected food options/supplements remain extra and are computed by the server.
- Cart `PUT /api/cart/items` includes `bargainingThreadId` to apply the agreement, with the existing product, quantity and selections.
- Reloads and quantity edits preserve that reference. Listed-price and negotiated lines remain distinct.
- Cart readback exposes `negotiatedUnitPriceCfa`, `negotiatedPriceExpiresAt` and the reference; the quote computes authoritative totals/revision.
- An invalid or expired referenced agreement makes the item unavailable. It never silently falls back to the catalog price.
- The customer must review the agreement or explicitly remove the item and re-add it at the listed price before requesting a fresh quote.

## Checkout timing

A new checkout must validate the accepted agreement before expiry and match the reviewed quote.
If the agreement expires before server checkout creation, the customer must review again.
A pending checkout already created with a valid agreement freezes its price snapshot and reference;
its stable retry can succeed after agreement expiry without creating a duplicate checkout.
The agreement window is not a claim about a payment-provider execution deadline.
Only normal verified payment reconciliation permits fulfillment.

## Current screens and statuses

Customer offer buttons open product detail. The panel loads persisted history, shows remaining
attempts and the merchant response, allows a price reply or acceptance/rejection, and provides refresh.
Only a server-accepted response displays the agreed price and expiry or attaches it to basket additions.
Loading, session errors and failed requests remain visible; an uncertain response is not local acceptance.

Statuses are `OPEN`, `REJECTED`, `ACCEPTED`, `EXPIRED`, `CANCELLED` and `LOCKED_ATTEMPTS_EXHAUSTED`.
`nextActor` controls who may reply while attempts remain. The customer UI displays expired/closed
states read-only; it does not promise a reset, reopen or cancellation control.
Vendor inbox/actions are merchant-scoped. Neither side sees another customer's or merchant's threads.
Refresh is the MVP update mechanism; this flow does not promise new push notifications, expiry
reminders, countdowns or delivery guarantees.
