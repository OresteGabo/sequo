# Returns MVP flow

The signed SEQUO MVP PDF defines scope. This document records the implemented Task 19
customer request and readback flow; it does not approve additional transport or refund rules.

## Server eligibility

- The return window is 72 hours from the persisted delivery time. A request exactly at the deadline is within the window.
- The server uses its own clock, delivery record, authenticated owner and approved policy configuration.
- The customer app displays `eligible`, the server reason and `returnWindowEndsAt`; it does not calculate eligibility from the device clock.
- The MVP request covers the whole order. Item selection and partial refund allocation are not part of this screen.
- The server derives the customer, refund ceiling and credential. Customer-supplied time, eligibility flags, refund amounts or PINs do not grant eligibility.
- The PDF does not impose a blanket food-return ban. Only approved configured return-category exclusions may reject an otherwise eligible request.
- Return eligibility and relay transport eligibility are separate decisions: food, perishables and sensitive items are excluded from relay routing, without inventing a blanket rejection of their return requests.

## Authenticated customer API

| Request | Purpose |
| --- | --- |
| `GET /api/returns/orders/{orderId}/eligibility` | Owned-order eligibility, deadline and routing instructions |
| `GET /api/returns/orders/{orderId}` | Persisted returns for the owned order |
| `GET /api/returns` | Customer's own return records |
| `POST /api/returns` | Request with `returnId`, `orderId` and `reason` only |

The request ID stays stable during an uncertain submission and its retry.
The app displays a reference only after a server response/readback confirms it.
If a return already exists for the order, the server returns its canonical ID even when a
retry proposes another ID. Reusing an existing ID with a changed reason is rejected.
Other customers' orders and return records remain inaccessible.

Eligibility includes the server `dropoffMethod`, `instructions`, actual `relayPoints` and any
`existingReturnId`. Return snapshots include the actual status, timestamps, instructions
and an owner-only `returnPin` when a valid relay credential is available; credential hashes
are not exposed. The app never generates a return PIN or marks a physical handoff complete.

## Routing and instructions

| Server method | Customer behavior |
| --- | --- |
| `RELAY` | Use the available eligible points and server-issued credential shown for this return |
| `SEQUO_INSTRUCTIONS_PENDING` | Keep the packaged item and reference; obtain approved SEQUO instructions before moving it |
| `CUSTOMER_MANAGED_PENDING` | Record the Click and collect request; await SEQUO's agreed customer-managed return procedure |
| `CUSTOMER_MANAGED` | Follow the approved seller-arranged instructions; this does not authorize relay drop-off |
| `ALREADY_DEPOSITED` | Read the recorded status and subsequent instructions; no pending drop-off prompt |

An eligible request without an available or eligible transport route can be recorded as
`Requested`, with pending instructions and no invented PIN or destination.
Click and collect remains `PENDING` until SEQUO agrees a policy. The customer app adds no
new product exclusion, collection fee or transport rule to resolve that pending decision.
A configured, approved seller-managed route uses the actual server instructions.
Legacy records without a usable owner credential require SEQUO confirmation, not a guessed PIN.

The 72-hour deadline governs requesting a return. This screen does not invent a separate
drop-off deadline, an automatic closure or an extension of the request window.

## Customer screen and persisted states

Open Orders, expand **Return options and history**, and refresh for server eligibility and
existing records. An eligible customer enters a reason and submits the whole-order request.
An uncertain request offers a retry of the same ID and reason, or refresh to read its result.
Reloads show the canonical reference, actual status, routing instructions and recorded timestamps.
Known instruction text is localized in English/French; actual point names and unknown server
instructions are preserved. Failed reads do not become local eligibility or fabricated progress.

Supported return states are `Requested`, `AwaitingRelayDropoff`, `DroppedAtRelay`,
`ReceivedBySequo`, `RefundPending` and `Rejected`. The UI shows the returned state rather
than synthesizing collection, inspection, acceptance or refund-completed milestones.
The old local request/reference/PIN simulation is not used by the real order-history flow.

## Receipt and refunds

A request, relay drop-off or pending refund record does not confirm a completed refund.
SEQUO receipt and inspection are required before a refund decision; authorized operational
steps and verified provider execution remain separate from this customer request screen.
The MVP does not trigger refunds automatically on inspection, promise provider completion,
or introduce cash refunds, cash withdrawal or a new ledger policy.
