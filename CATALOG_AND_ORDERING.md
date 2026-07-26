# Catalog And Ordering

This document defines how the Sequo Customer App presents products, routes baskets, validates seller media, and creates customer orders.

## Product Models

Sequo supports two customer ordering models.

| Model | Applies to | Customer expectation | Logistics |
| --- | --- | --- | --- |
| Fast delivery | Food, drinks, fresh groceries, perishables, urgent essentials | Fresh item, quick preparation, immediate dispatch | Seller prepares now, rider collects when ready |
| Sequo grouped logistics | General goods, non-urgent items, multi-seller cooperative baskets | Amazon-like package flow, grouped delivery, stronger tracking | Sellers prepare sub-orders, Sequo consolidates and routes as one package |

Food/perishable and general-goods products must not share the same detail-page assumptions. Food customization is about preparation choices. General product selection is about variants, stock, delivery mode, and return/bargaining eligibility.

## Catalog Item Requirements

Every customer-visible item must include:

- Seller or cooperative identity.
- Category and product type.
- Current price in CFA.
- Availability status.
- Delivery eligibility.
- Real-time seller photo status.
- Return eligibility indicator.
- Bargaining availability when enabled by the seller.
- Last updated timestamp for price, stock, and photo.

General goods should include weight, dimensions if relevant, stock count or stock state, variant options, and handling requirements.

Food and perishables should include preparation time, option groups, allergen/safety notes when available, freshness timing, and whether the item can travel by fast delivery.

## Real-Time Product Photos

Sellers must upload real-time camera pictures for listed products.

Rules:

- Camera capture is required for seller-specific product photos.
- Gallery upload is prohibited.
- Google image upload is prohibited.
- Reused internet photos are prohibited.
- The app may allow generic catalog imagery only for truly generic sealed items, such as bottled water, where a real-time photo adds little trust value.
- Customer-facing UI should show a trust cue such as `Photo captured today` or `Real-time seller photo` when the backend confirms the image source.
- Product ranking may favor listings with fresh verified photos.

Customer app implications:

- Never label a product as photo-verified unless the backend has verified the capture source.
- If a product photo is stale, missing, or unverified, show the product but reduce trust cues and prompt the customer to inspect details.
- For high-risk categories, backend policy may hide products until a valid real-time photo exists.

## Food Options Pattern

Food items use preparation options, not normal product variants.

Use option groups such as:

| Option group | Example | Selection behavior |
| --- | --- | --- |
| Base choice | Rice, attieke, fries | Required single choice |
| Sauce | Tomato, peanut, spicy | Optional or required single choice |
| Toppings | Extra chicken, egg, cheese | Optional multiple choice with price deltas |
| Spice level | Mild, medium, hot | Single choice |
| Instructions | No onions, call before arrival | Free text with moderation and length limits |

Rules:

- Each group defines `minSelections`, `maxSelections`, and whether it is required.
- Paid options must show price deltas before adding to basket.
- The basket item snapshot must store the chosen options and price at the time of order.
- Food options cannot be represented as color/size variants.
- Food option availability can change by seller and by time of day.

## General Goods Variant Pattern

General goods use normal product variants.

Examples:

- Size.
- Color.
- Storage capacity.
- Pack quantity.
- Condition.
- Seller-specific SKU.

Rules:

- Each variant can have its own price, stock, photos, and delivery eligibility.
- Variant choice must happen before add-to-basket when it affects price or stock.
- Bargaining can be enabled or disabled per product or variant according to backend policy.
- Return eligibility must be visible before checkout.

## Basket Routing

The backend should classify each basket line into one of the following routes.

| Basket route | Trigger | Customer result |
| --- | --- | --- |
| Fast food/perishable | Restaurant, hot meal, fresh product, urgent perishable | Immediate preparation and delivery flow |
| Fast general goods | Eligible nearby physical goods | Express-style delivery if seller, distance, weight, and operations allow it |
| Grouped Sequo logistics | General goods, cooperative orders, non-urgent grouped items | Sequo package flow with grouped delivery |
| Pickup or Click & Collect | Seller enables pickup and customer selects it | No rider delivery fee |

The customer should not need to understand internal logistics terms. The UI should explain outcomes: `Fast delivery`, `Grouped Sequo delivery`, `Pickup`, or `Point de Relai`.

## Grouped Seller Consolidation

Orders containing multiple items from grouped sellers in the same area must be routed through Sequo as a single package.

Example: sellers in a Marche de Mulhouse cooperative can appear as one customer-facing shopping group. Internally:

- Each seller owns only their sub-order.
- Sequo keeps seller-level accounting separate.
- Sequo operations consolidate sub-orders into one package.
- The customer tracks one grouped order/package.
- Delivery fees are calculated for the consolidated delivery route, not as separate customer-facing deliveries for every seller.

Customer-facing rules:

- Show the cooperative or grouped-market identity clearly.
- Show which item comes from which seller when useful for trust.
- Make one payment for the consolidated customer order.
- Show one package timeline once the grouped order enters Sequo logistics.

## Checkout Requirements

Before payment, the customer must see:

- Item subtotal.
- Option/variant adjustments.
- Delivery route.
- Delivery fee before discounts.
- Subscription delivery discount.
- Referral delivery credit applied.
- Final amount to pay.
- Return eligibility summary.
- Bargaining price if a negotiated price is active.
- Payment provider choice: Yas Togo or Moov Africa.

The checkout action can create an order draft, but the order must not be completed without validated payment.

## Order Statuses

Customer-facing status names should stay simple.

| Status | Meaning |
| --- | --- |
| Payment pending | Customer started checkout but provider validation is not complete |
| Paid | Payment is validated and order can move forward |
| Waiting for seller | Seller must accept, reject, or adjust according to policy |
| Being prepared | Seller accepted and is preparing the order |
| Ready for pickup | Seller or relay has the item ready for the next handoff |
| Rider assigned | A rider or Sequo logistics mission is assigned |
| On the way | Delivery is actively moving toward the customer or relay |
| Available at Point de Relai | Package can be collected at relay point |
| Delivered | Customer received the package or completion proof was validated |
| Return requested | Customer opened a return within the policy window |
| Refund processing | Return was accepted and refund is being issued |
| Cancelled | Order was cancelled before completion |
| Problem reported | Support or operations must intervene |

## Data Snapshots

At checkout, the backend must snapshot:

- Seller and product IDs.
- Product name and category.
- Product photo reference and verification state.
- Selected options or variants.
- Quantity.
- Unit price and negotiated price if applicable.
- Delivery route and fee.
- Discounts and credits applied.
- Payment provider and payment reference.
- Return eligibility at purchase time.

Snapshots protect customers and sellers when catalog data changes after ordering.

## Acceptance Criteria

- Food and general goods render with distinct detail-page patterns.
- Seller photos display real-time verification status.
- Basket routing is deterministic and server-confirmed.
- Consolidated cooperative orders produce one customer package timeline.
- Payment validation is required before order completion.
- Customer sees all delivery fees, discounts, and credits before paying.
- Order snapshots preserve the exact purchase context.

