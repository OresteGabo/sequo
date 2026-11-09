# UI Patterns

This document defines customer-facing UI patterns for the Sequo Kotlin Multiplatform app.

## Design Principles

- French-first, simple language, with clear room for English and Ewe later.
- Operational clarity over decorative complexity.
- Trust cues should be visible: verified seller, real-time photo, delivery fee, payment status, return deadline.
- Customer should always know what happens next.
- Critical actions must protect against duplicate taps and unstable network.
- Money, distance, payment, bargaining, and return states must come from backend-confirmed data.

## Home

The home screen should prioritize:

- Nearby open sellers.
- Fast food/perishable ordering.
- Grouped seller/cooperative shopping.
- Popular general goods.
- Current subscription/referral delivery discount state.
- Search.

Avoid a marketing landing page inside the app. The first screen should help the customer order.

## Seller And Cooperative Cards

Cards should show:

- Seller or cooperative name.
- Category.
- Neighborhood or market area.
- Verified status.
- Open/closed state.
- Delivery modes.
- Rating or quality signal if available.
- Real-time photo coverage signal where relevant.

Grouped sellers should be labeled as one shopping group while still allowing item-level seller transparency.

## Product Detail: Food And Perishables

Food detail pages need a custom customization pattern.

Required sections:

- Food photo and freshness/trust cue.
- Seller name and preparation status.
- Base price.
- Preparation time.
- Required option groups.
- Optional toppings and extras.
- Spice or preparation preferences.
- Special instruction field.
- Fast delivery fee estimate.
- Add to basket.

Controls:

- Required single-choice groups should use radio or segmented selection.
- Optional add-ons should use checkboxes or steppers when quantity matters.
- Price deltas must be shown beside paid options.
- Required options must be completed before add-to-basket.

Food options must not look like normal color/size product variants.

## Product Detail: General Goods

General goods pages should show:

- Real-time camera photo status.
- Product title.
- Seller or cooperative.
- Listed price.
- Bargaining entry point if enabled.
- Historical minimum accepted price if available.
- Variant selectors such as color, size, quantity, condition, or pack size.
- Stock status.
- Delivery mode eligibility.
- Return eligibility.
- Add to basket.

Controls:

- Use swatches for color.
- Use steppers for quantity.
- Use menus or chips for size/variant sets.
- Disable unavailable variants instead of hiding them when that helps customer understanding.

## Real-Time Photo UI

Photo labels should be backend-driven.

Recommended states:

| State | UI treatment |
| --- | --- |
| Verified real-time photo | Strong trust badge |
| Generic approved image | Neutral badge for generic sealed items |
| Missing photo | Neutral empty image treatment and lower trust |
| Stale or unverified photo | Warning/neutral trust cue |

Do not show a photo as verified because it looks realistic. Verification must come from capture metadata or backend policy.

## Basket

The basket should group items by route:

- Fast delivery.
- Grouped Sequo delivery.
- Pickup or Click & Collect.

For each group, show:

- Items and seller source.
- Options or variants.
- Negotiated price if active.
- Delivery route.
- Delivery fee estimate.
- Return eligibility.
- Payment requirement.

If grouped sellers will be delivered as one package, the UI should make that consolidation obvious.

## Delivery Fee Breakdown

Show delivery fees as a small breakdown, not as a hidden final number.

Required rows:

- Delivery distance.
- Base delivery fee.
- Subscription discount.
- Referral delivery credit.
- Final delivery fee.

Use [DELIVERY_PRICING.md](DELIVERY_PRICING.md) as the calculation source.

## Payment

Payment screen requirements:

- Provider selector for Yas Togo and Moov Africa.
- Exact amount due.
- Payment pending state.
- Retry failed payment.
- Cancel pending payment when allowed.
- Clear success state after backend validation.

The UI must not show an order as complete until payment and completion conditions are validated by the backend.

## Bargaining

Product page requirements for eligible products:

- Show `Propose price` action.
- Show attempts remaining.
- Show seller response.
- Show historical minimum accepted price when available.
- Show 24-hour lock countdown after acceptance.
- Show checkout action with accepted price.

Do not render bargaining controls for products where the seller disabled bargaining.

See [BARGAINING_FLOW.md](BARGAINING_FLOW.md).

## Order Tracking

Order tracking should be timeline-based:

- Payment pending.
- Paid.
- Waiting for seller.
- Being prepared.
- Ready for pickup.
- Rider assigned.
- On the way.
- Available at Point de Relai.
- Delivered.
- Return/refund states when applicable.

Critical events should support push notification and SMS fallback where backend operations enable it.

## Returns

Delivered order screens must show:

- Return eligibility.
- Exact 72-hour return deadline.
- Return request action.
- Point de Relai drop-off instructions.
- Return reference/PIN after request creation.
- Refund progress after inspection acceptance.

See [RETURNS_POLICY.md](RETURNS_POLICY.md).

## Empty And Error States

Required states:

- Loading.
- Poor connection.
- Payment pending.
- Payment failed.
- Seller unavailable.
- Product out of stock.
- Bargaining unavailable.
- Return window expired.
- No nearby sellers.
- No orders yet.

Every error state should give a next action: retry, edit address, choose another provider, contact support, or continue browsing.

Confirmed empty results use the shared illustrated `SequoEmptyState`: a contextual drawing, a clear title, a short explanation, and an action only when it leads somewhere useful. Cart and Orders open shopping; search can clear the query; filtered catalog views reset the selection; archived notifications return to the inbox. Saved-address placeholders must not offer an Add button until an address creation flow exists.

Never show an empty result in place of loading, an expired session, or a failed request. Keep empty-state text readable in both themes, allow translated text to wrap, and use the compact layout inside existing cards or beneath search filters.

## Navigation Ownership

- Bottom navigation owns the main destinations: Markets, Cart, Home, Orders, and Settings.
- The header bell opens notifications. Do not repeat Cart, Orders, alerts, or Profile in the side menu.
- Settings contains the profile, sign-in/sign-out, language, appearance, saved addresses, delivery benefits, and legal policies. Language and appearance remain available to guests.
- The side menu helps customers choose a shopping intent: nearby shops with distance information, discounted products, negotiable products, and the live catalog categories. Help opens the official support page.
- Category and shopping-intent filters apply to individual products, including shops selling several categories. Empty results offer a reset action.
- Keep different product types in the same main navigation. Adapt their product details and ordering options rather than adding a bottom tab for every category.
- Useful future shortcuts are Buy again (a reorder flow from completed orders) and Saved shops (persisted favorites). Add these only when their data and actions are implemented.

## Accessibility

- Minimum touch targets should be comfortable on low and mid-range Android devices.
- Text must support system font scaling.
- Color must not be the only status indicator.
- Icons need labels for screen readers.
- Payment and refund statuses should be written in plain language.
- Amounts should be formatted consistently in CFA.

## Compose Implementation Notes

Prefer reusable components:

- `MoneyText`.
- `DeliveryFeeBreakdown`.
- `VerifiedPhotoBadge`.
- `FoodOptionGroup`.
- `VariantSelector`.
- `BargainPanel`.
- `PaymentStatusPanel`.
- `ReturnDeadlineBanner`.
- `OrderTimeline`.
- `EmptyState`.
- `ErrorState`.

Domain logic should live outside composables. Composables should render backend-confirmed state and dispatch user events to view models.

## Shopping Visual Hierarchy

Use flat theme-aware surfaces, 20–24 dp corners, subtle dividers, and consistent 16 dp card padding. Reserve strong green for the selected destination, prices, and primary actions. Avoid decorative shadows, nested product cards, duplicate category labels, and controls without working actions.

Home product cards give the photo, two-line name, location, price, and Add button separate space. Bargaining remains a distinct action. Categories retain stable ordering while browsing, with a small pin marker for pinned items. Markets uses anchored sort and filter menus so choices do not displace shop listings. Shop distance, estimated delivery fee, and time form a compact metadata row.

Product details use one clear hero image, actual photo actions, readable pricing with wrapping for large text, and radio or checkbox rows for options. Cart quantities and payment options use comfortable touch targets; totals and order status have a clear visual hierarchy. Main navigation stays compact and uses a small selected indicator without glow or heavy elevation.
