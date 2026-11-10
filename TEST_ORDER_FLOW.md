# Testing Sequo: customer → vendor → rider → relay pickup

These steps use the normal deployed apps and database at **https://api.sequoservice.com**. All sample shops, products, riders and hubs are labelled `[TEST]`.

## Installation status

The updated normal Sequo, SequoVendor, SequoHub and SequoRider iPhone builds are installed. On 10 November, the old Vendor app data was backed up privately and its installation was replaced with the paid-team signed build after owner approval. Server shops/orders were preserved. Vendor may require signing in again; use its normal **Sign in to a test account** entry for the fictional accounts below.

## Private sign-in details

Open [the private test account list](demo/.private/TEST_ACCOUNTS.md). It contains usernames, passwords, their assigned shops/hubs and the exact expiry date. It is stored locally, excluded from Git, and should not be shared publicly.

In each app's sign-in screen, choose **Sign in to a test account**, then enter that app's username and password. French: **Connexion au compte de test**. This is a password-protected sign-in to real server records, not the local-demo or passwordless-fixture buttons.

Use these accounts for your first test:

| App | Username | Assignment |
| --- | --- | --- |
| Sequo | `customer.flow` | Dedicated fictional customer with a saved Lomé test address |
| SequoVendor | `vendor.mode` | `[TEST] Mode Demo` shop |
| SequoRider | `rider.ami` | `[TEST] Ami` test rider |
| SequoHub | `hub.tokoin` | `[TEST] SequoHub Tokoin` |

You can also keep your usual Apple login in the customer app. The dedicated customer keeps practice orders separate from your personal account history. Passwords are in the private file, not this guide. Signing out and back in lets you switch test identities.

## 1. Place a single-shop test order in Sequo

1. Sign in as `customer.flow`.
2. Open **Mode Demo** and choose **T-shirt essentiel** (or another available product in that shop).
3. Select an available size and colour, then add it to your cart.
4. Keep the first basket to **one shop**, containing only `[TEST]` products. Mixed real/test baskets are refused.
5. At checkout, select the saved test address in Lomé. Test baskets can use a fictional Tokoin quote origin when normal routing is not configured.
6. Choose **Point de Relai / relay pickup**, then **[TEST] SequoHub Tokoin**.
7. Review the displayed CFA prices and select **Place test order · no charge**.
8. Open **Orders** and tap **Confirm test payment** in the labelled test-order panel.
9. Keep the order reference handy. Payment confirmation can be retried safely; it does not create a second order.

**Expected:** the order appears in the vendor's normal incoming-order queue. No wallet is contacted, no money is moved, and there is no provider payment receipt. A normal product or another customer's checkout cannot use this confirmation.

## 2. Prepare it in SequoVendor

1. Sign in as `vendor.mode`.
2. Open **Orders** and refresh the incoming list.
3. Open the matching `[TEST]` order/reference.
4. **Accept** the order.
5. **Start preparation**.
6. **Mark packed / ready for pickup**, entering one package for this first test.

**Expected:** the same quantities and selected size/colour are retained, and the customer order waits for rider assignment. The vendor can operate only its assigned shop. Accept promptly: normal merchant response deadlines still apply.

## 3. Assign the test rider from Sequo

1. Return to the customer's **Orders** screen.
2. Refresh the order/test-dispatch panel.
3. When the vendor has marked it ready, select **[TEST] Ami**.
4. Tap **Assign test rider**.

This bounded test-dispatch action can operate only your own no-money test order and the four seeded test riders. It does not require giving the customer a global administrator role. The first guided test supports one shop; multi-shop consolidation remains a separate operations workflow.

**Expected:** the mission is offered to Ami. If the rider is busy or paused, finish their earlier mission or select another available test rider and sign in to that rider's account in SequoRider. Capacity and pause checks remain active.

## 4. Collect and deposit in SequoRider

1. Sign in as `rider.ami` and refresh **Missions**.
2. Open the matching offered mission and **Accept** it.
3. Confirm the seller/package details, provide the requested pickup proof/metadata, and complete **Pickup**.
4. Open the relay destination and choose **[TEST] SequoHub Tokoin**.
5. Coordinate the logical slot with the hub screen; choose an available slot such as **TEST-A1**.
6. Provide the requested relay handoff confirmation/proof, then complete **Relay deposit**.

Use an explicitly labelled test parcel/prop when trying camera or handoff screens. These are fictional orders; do not record a real delivery claim. Do not use the local sample/demo workspace, since it does not change the deployed order.

**Expected:** the mission becomes deposited at the relay, the slot becomes occupied, and the customer receives a server-generated pickup credential. Refresh screens after changing apps.

## 5. Release the parcel in SequoHub

1. Sign in as `hub.tokoin`.
2. Open the assigned Tokoin hub and refresh its slots/parcels.
3. Confirm the order/package is in the selected logical slot.
4. In the customer Sequo app, open **Orders → order details → relay pickup** to display the numeric pickup code or QR.
5. In SequoHub, use **Manual QR / pickup code** (or the available scanner) and enter/scan that customer credential.
6. Review the matching parcel, complete the requested identity/handoff confirmation for this fictional test, and confirm release.

**Expected:** the customer order becomes **Delivered**, the mission becomes **Released by relay**, and the slot becomes available again. A wrong code, wrong hub operator, missing identity confirmation or a reused credential must not release the parcel.

## Other available accounts and scenarios

The private file lists all twelve vendor owners, four shop staff, six hub operators and four riders. Each owner/staff profile is scoped to one shop, each operator to one hub, and each rider to its own assignments.

- **Sizes/colours:** `vendor.mode`, `vendor.petits`, `vendor.pas`.
- **Amounts:** `vendor.grains` / `vendor.marche` for kg, `vendor.boissons` for litres, `vendor.tissus` for metres.
- **Meal options:** `vendor.bistro` / `vendor.saveurs`. Meals and grocery products retain the normal relay exclusions; test these with home delivery instead of the first relay journey.
- **Home delivery:** retains its normal delivery-PIN/proof requirements. Use the relay journey above for the first complete four-app exercise; home PIN administration is a separate normal operations step.
- **Wrong-role isolation:** another vendor must not see this shop's order; another hub operator must not release this hub's parcel; another rider must not complete Ami's mission.

## What this test changes

Orders, preparation, assignments, custody transitions and pickup-code use are persisted in the existing live database. Test orders are marked by a server-owned `TEST_NO_FUNDS:` reference and shown as test orders in Sequo. They do not consume referral/subscription benefits, create merchant/rider payout accruals, assess relay storage fees, or create refundable funds. Ordinary checkout and provider authentication remain separate.

The test login and no-money flow are explicitly enabled deployment settings. Credentials expire after 30 days; the exact date is in the private file. Password expiry stops new sign-ins. To end existing sessions immediately, revoke them or deactivate the test identities through the normal account administration tools.

## Troubleshooting and cleanup

- **No test sign-in button:** use the updated normal app, confirm internet access, and reopen the sign-in screen. The button appears only when the server enables the feature.
- **Login refused:** check the username's app, password and expiry. Extra ordinary-shop/hub permissions or elevated roles intentionally block test-password login.
- **No order in Vendor:** confirm the test payment, choose the correct vendor, refresh, and compare the order reference.
- **Cannot assign:** mark the package ready in Vendor first; use one shop; check whether the selected rider is already busy.
- **No relay option:** use eligible non-food goods and the same test hub in the customer, rider and hub apps.
- **Pickup code unavailable/expired:** refresh the customer's order and use its normal renew-credential action if offered. Do not invent a PIN.

Record IDs for later cleanup are in [the catalog manifest](demo/live-catalog/seed-manifest.json) and [the vendors/hubs manifest](demo/live-catalog/vendors-and-hubs-manifest.json). Unpublish test products and disable test accounts when finished. Preserve referenced order/audit history instead of blindly deleting rows. Deployment settings `TEST_ACCOUNT_LOGIN_ENABLED=false` and `TEST_ORDER_FLOW_ENABLED=false` disable new test entry points; revoke existing test sessions/deactivate their users as part of final cleanup.

## Verification recorded

The deployed API verified all 27 private logins and completed the full single-shop relay journey over its normal HTTP endpoints. It confirmed zero merchant payout accruals, rider settlements, financial ledger entries, provider payment receipts and benefit-consumption records, and the relay slot became available again. [Verification metadata](demo/live-catalog/test-flow-verification.json) contains only non-secret IDs/results. Scripted custody was labelled as a test; this is not a claim of a human physical delivery.


## iPhone cart and checkout verification — 10 November 2026

The normal Sequo build was installed and exercised through its actual UI on the connected iPhone using `customer.flow`. Clothing size/colour isolation, shoe sizes, measured portions, repeated quantity changes, removal, empty cart, address creation/editing/switching, temporary location access, relay selection/eligibility and home/relay no-money checkout passed. The phone was left signed into this test customer with an empty cart. The personal account's existing basket was not changed.

In **Cart → Home delivery**, use **Add address** or the edit icon beside a saved address. Enter the recipient/contact details and a precise delivery pin: use **Use my current location** or paste a full Apple Maps, Google Maps or OpenStreetMap coordinate link. Shortened links and place-name-only searches cannot supply a precise pin. Saving an edited address refreshes the delivery quote before checkout becomes available. Location permission is optional; cancelling the form discards its changes.

Two fictional orders were created and test-confirmed on the phone: home delivery, 13,900 CFA (two L/Sable shirts and one S/Indigo shirt), and Tokoin relay pickup, 6,900 CFA (one size-43 shoe item). No money moved; merchant/rider settlements, payout accruals, financial ledger entries and provider webhooks were zero for these orders. The home order later reached the normal merchant-response cancellation deadline; fulfillment was not part of this cart exercise. Create a fresh order and accept promptly when continuing the vendor/rider/hub journey.

The test found and fixed missing cart address controls, navigation screens retaining another screen's scroll position, and an old quote remaining after editing the currently selected address. Native builds and five focused client regression tests passed. Detailed physical-phone evidence and the reusable harness are recorded in the backend repository under `docs/verification/cart-phone-2026-11-10.md` and `scripts/cart-iphone-tests/`. Raw phone screenshots, logs, credentials and device identifiers remain private.


## Reuse a product across shops

In **SequoVendor → Catalog → Add product/Edit**, start typing the product name (at least two characters). Select a shared suggestion such as **Lait**. A **Linked product** label confirms the association; **Unlink** removes it. Keep the brand, pack size, description and options accurate for your own offer. Prices, stock and photographs remain specific to your shop.

Without a suggestion, saving reuses a complete matching name or creates a reusable reference for that complete name. Suggestions recognize common French/English names and accents. Sequo customer search includes those shared names/aliases and groups linked offers from different shops while retaining each shop's listing and price. A common family such as Lait does not mean that different brands or package sizes are identical goods.


## Search filters and sorting

Sequo search now puts **All / Deals / Negotiable** directly below the search field. Use **Relevance**, **Price: low to high**, **Price: high to low**, or **Nearest** from the sort menu. Nearest appears when the catalog provides valid shop distances. Prices follow the unit/pack shown on each listing; shop offers remain separate.

If a filter leaves no results, **Show all offers** resets it while keeping your search text. For a quick check, search `riz` and compare the two price orders; search `lait` and choose Deals to see the current milk promotion. The redundant search headings have been removed. The update was installed and checked through the actual iPhone UI without changing the cart or placing orders.


## Choose a presaved essential in SequoVendor

Open **Catalog → Add product → Choose from essentials**. Milk (**Lait**), beans (**Haricots**), rice and other common staples appear first. Browse Food & groceries, Household, Personal care or Baby, or search French/English names such as `milk` or `beans`. Choose a product, then enter your own offer's price, brand/pack details, stock and sizes/amounts. Save normally; the chosen shared ID is retained.

The starter list contains 74 shared families. A shop offer points to the shared product through `products.product_reference_id`: one Lait master record can serve 100 shop offers with different prices. Choosing a preset creates no shop listing until you save. The full reviewed list is in the backend repository at `docs/catalog/ESSENTIAL_PRODUCTS.md`; custom products remain available when a good is absent from the starter catalog.


Existing grocery listings have also been linked to the reviewed essential families. Names such as milk/`lait`, beans/`haricots`, and rice/`riz` can now find the linked listings through shared aliases. Seller prices and pack/unit details remain visible and sortable. There is currently one milk listing; additional shops must list milk before the app can show cross-shop milk prices. Prepared meals, juices, mixed baskets and litre-sold ginger drink retain their separate identities.

## KKiaPay sandbox (awaiting merchant keys)

The updated checkout also supports KKiaPay for Yas Togo (Mixx) and Moov Africa Togo (Flooz). Payments remain disabled until the company's account and keys are ready. Manual no-money simulation still works for the seeded test products.

When sandbox is configured, select the KKiaPay option for a `[TEST]` basket. Complete the provider's synthetic Togo wallet flow, then return to Orders and use **Check payment**. **Continue payment** reopens an unfinished payment. Wait and check before authorizing another attempt if a payment is pending. A KKiaPay sandbox checkout cannot use the manual confirmation button. Continue the vendor/hub/rider journey after provider verification.

The backend setup and activation guide is `sequo-api/docs/KKIAPAY_SETUP.md` in the sibling backend repository. No real KKiaPay transaction has been verified yet; keys and merchant activation are pending.
