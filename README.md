# Sequo Customer App

Sequo Customer App is the Kotlin Multiplatform mobile client for the Sequo marketplace in Lome, Togo. The app lets customers browse trusted local sellers, order food and perishable goods for fast delivery, order general goods through Sequo-style grouped logistics, pay with supported mobile-money providers, track orders, negotiate eligible item prices, use delivery discounts, and request returns through a Point de Relai.

This repository is currently a Kotlin Multiplatform starter targeting Android and iOS. The product rules in this README and the linked Markdown files are the source of truth for the next implementation phase.

## Product Positioning

Sequo is not a generic shopping app. It combines two commerce models:

1. Fast local delivery for food, drinks, groceries, and perishables where freshness, preparation status, and immediate rider assignment matter.
2. Amazon-like delivery for general goods where items can be grouped, routed through Sequo logistics, stored at relay points, and delivered as one package.

The customer app is one interface in a broader platform:

| Interface | Actor | Customer-app dependency |
| --- | --- | --- |
| Sequo Client | Customers | This repository |
| Sequo Pro | Sellers and merchants | Product availability, photos, bargaining, order acceptance |
| Sequo Admin | Sequo operations | Logistics grouping, returns inspection, disputes, payment audit |
| Sequo Livreur | Riders | Pickup, delivery PIN, delivery status |
| Sequo Relai | Relay partners | Package pickup, customer returns, relay storage |

## Current Business Rules

These rules override older legacy documentation.

| Area | Rule |
| --- | --- |
| Catalog | Food and perishables use a fast-delivery model. General goods use a grouped Sequo logistics model. |
| Food customization | Food items use topping/option groups, not normal product variants. See [UI_PATTERNS.md](UI_PATTERNS.md). |
| Product photos | Sellers must upload real-time camera photos. Gallery uploads and Google images are prohibited except generic items such as sealed bottles of water. |
| Delivery fee | Delivery is 400 CFA for 5 km or less, then +100 CFA for every extra km. See [DELIVERY_PRICING.md](DELIVERY_PRICING.md). |
| Subscriptions | Paying subscribers receive automatic percentage discounts on delivery fees. Multi-year subscribers receive deeper discounts than monthly subscribers. |
| Payment | Payment must be validated before order completion through Yas Togo or Moov Africa. Cash collection and cash withdrawals are not supported. |
| Referral | Parrainage gives 500 CFA of delivery-service discount credit. It is not a cash payout and cannot be withdrawn. |
| Bargaining | Eligible products can support price proposals, max 3 attempts per party, with agreed minimum prices locked for 24 hours. See [BARGAINING_FLOW.md](BARGAINING_FLOW.md). |
| Consolidation | Items from grouped sellers in the same area, such as a Marche de Mulhouse cooperative, are routed through Sequo as one package while preserving seller-level accounting. |
| Returns | Returns are allowed within 72 hours of delivery through Point de Relai drop-off. Refunds trigger automatically after Sequo inspection accepts the product. See [RETURNS_POLICY.md](RETURNS_POLICY.md). |

## Documentation Map

| File | Purpose |
| --- | --- |
| [CATALOG_AND_ORDERING.md](CATALOG_AND_ORDERING.md) | Product types, basket routing, real-time photos, checkout, status rules, grouped sellers |
| [DELIVERY_PRICING.md](DELIVERY_PRICING.md) | Delivery fee formula, examples, subscription discounts, customer display rules |
| [PAYMENTS_AND_REFERRALS.md](PAYMENTS_AND_REFERRALS.md) | Yas Togo and Moov Africa payment validation, no-cash rules, referral delivery credit |
| [BARGAINING_FLOW.md](BARGAINING_FLOW.md) | Negociation flow, attempt limits, lock windows, historical minimum accepted prices |
| [RETURNS_POLICY.md](RETURNS_POLICY.md) | 72-hour return window, Point de Relai drop-off, inspection, automatic refund trigger |
| [UI_PATTERNS.md](UI_PATTERNS.md) | Customer app UI patterns for catalog, food options, checkout, returns, low-network states |

## Legacy Material Reviewed

The new docs were created after reviewing the legacy Sequo repository at `/Users/muhirwagabooreste/AndroidStudioProjects/Sequo`, including its README, product validation notes, market notes for Lome, features and requirements, order lifecycle, role permissions, task backlog, technical README, original French cahier des charges, and PlantUML/auth documentation. Useful legacy concepts were retained, especially verified sellers, landmark-based addresses, delivery PINs, Point Relai logistics, grouped logistics, seller/rider/admin role boundaries, low-connectivity support, and auditability.

Older assumptions that conflict with the current rules were intentionally replaced. Notably, the new customer app docs do not allow cash on delivery, do not use the older 14-day return window, and do not treat referral rewards as withdrawable money.

## Customer MVP Scope

The first production customer scope should support:

- Phone-based account creation and secure sign-in.
- French-first customer experience with language architecture that can later support English and Ewe.
- Guest browsing if enabled by the backend, but authenticated checkout.
- Browse sellers, cooperatives, products, categories, and nearby availability.
- Food detail pages with topping/options groups.
- General goods detail pages with variants, seller real-time photos, bargaining if enabled, and return eligibility.
- Delivery address capture using neighborhood, landmark, phone contact, notes, and optional map position.
- Cart and basket routing for fast delivery and grouped Sequo logistics.
- Delivery fee calculation with subscriber and referral delivery discounts.
- Payment through Yas Togo or Moov Africa before order completion.
- Order tracking with human-readable statuses.
- Delivery PIN display where required.
- Return request creation within 72 hours of delivery.
- Support entry points for wrong items, damaged products, delivery problems, payment problems, and refund tracking.

## Order Lifecycle

1. Customer browses products or sellers.
2. Customer selects food options or general product variants.
3. Customer adds items to the appropriate basket.
4. App calculates item subtotal, delivery fee, subscription discount, referral delivery credit, and total.
5. Customer chooses Yas Togo or Moov Africa and submits payment.
6. Backend validates payment through provider callback or verified payment status.
7. Seller receives the paid order and accepts, rejects, or requests a supported correction.
8. For fast delivery, the seller prepares the order and a rider is assigned.
9. For grouped logistics, seller sub-orders are consolidated through Sequo operations and may pass through Point Relai infrastructure.
10. Customer tracks order status and receives critical notifications.
11. Order is delivered or picked up with required proof such as a delivery PIN.
12. Order can be rated, reported, or returned if within the return policy.

## Architecture Direction

The shared module should contain reusable product logic and Compose Multiplatform UI:

```text
shared/src/commonMain/kotlin/dev/orestegabo/sequo/
  core/          formatting, money, distance, errors, time
  data/          repositories, API DTOs, local cache
  domain/        basket routing, pricing, payment, bargaining, returns
  presentation/  screens, view models, UI state, navigation
  App.kt
```

Server-side truth is required for orders, payments, refunds, bargaining counters, delivery pricing, subscriptions, and audit logs. The mobile app must not be the authority for money or order completion.

## Repository Structure

| Path | Purpose |
| --- | --- |
| `androidApp` | Android application entry point |
| `iosApp` | iOS application entry point |
| `shared` | Shared Kotlin code, Compose UI, resources, and tests |
| `gradle/libs.versions.toml` | Dependency and plugin versions |

## Build Commands

Android debug build:

```bash
./gradlew :androidApp:assembleDebug
```

Android shared tests:

```bash
./gradlew :shared:testAndroidHostTest
```

iOS simulator tests:

```bash
./gradlew :shared:iosSimulatorArm64Test
```

## Implementation Priorities

1. Model money, distance, delivery fee, subscription discount, and referral credit in shared domain code.
2. Model basket routing for food/perishables, general goods, grouped sellers, and delivery modes.
3. Build product detail patterns for food options and normal product variants.
4. Add payment state handling for Yas Togo and Moov Africa.
5. Add bargaining UI and domain rules for eligible products.
6. Add return request flow with 72-hour eligibility checks and Point de Relai drop-off details.
7. Add robust loading, offline, retry, duplicate-submit protection, and payment-pending states.

## Security And Privacy

- Do not commit real API keys, payment keys, signing keys, OAuth secrets, database credentials, or private local paths.
- Phone numbers and exact addresses should be visible only to authorized actors.
- Seller, rider, and customer contact should use Sequo-mediated channels where possible.
- Payment callbacks must be authenticated, idempotent, and audited server-side.
- Financial changes, refunds, subscriptions, bargaining agreements, and admin overrides must be traceable.
