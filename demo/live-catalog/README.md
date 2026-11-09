# Test catalog in the normal deployed app

On 9 November 2026, at the owner's explicit request, 12 test shops, 120 labelled products and four test rider records were added to the existing live database used by `https://api.sequoservice.com`.

The normal Sequo iPhone app is installed with the current Settings improvements. The separate Sequo Demo app was removed. The separate demo API/database containers were stopped and their volume retained.

`seed-manifest.json` lists the imported record IDs for later cleanup. All use the `demo-v1-` namespace; storefront/product/account display names use `[TEST]`. Existing production records were preserved. A private database backup and rollback validation preceded insertion. Test order, payment, admin/customer identity, token and settlement history were not copied.

Images are now served independently by the normal HTTPS host under `/test-catalog/`. They do not depend on the stopped demo API. Images remain labelled illustrations, and camera verification stays false.

Normal payment validation, merchant account scoping, dispatch and photo requirements still apply. This catalog import does not connect wallet providers or enable synthetic payment confirmation in the normal app. Food listings requiring camera evidence need genuine seller capture before ordinary checkout can accept them.

For cleanup, first unpublish the manifest's products/storefronts and disable unused test rider records. Preserve any resulting real order references and history; do not blindly delete merchant/user rows referenced by orders or audit trails. Hard deletion can be reviewed when testing is finished.

## Vendor profiles and relay hubs

The follow-up live dataset completes the twelve shops' named vendor-owner profiles and adds four scoped vendor staff profiles. Six `[TEST] SequoHub` relay points are configured in Tokoin, Assigame, Adidogome, Hedzranawoe, Agoe and Be-Kpota, each with its own assigned `RELAY_PARTNER` operator. Each has two logical shelves and twelve available logical slots (72 total), with the existing default active/always-open test configuration.

`vendors-and-hubs-manifest.json` records the exact users, memberships, points, grids and slots for later cleanup. Vendor owners remain scoped to their original shops; staff each belong to one shop; operators are each assigned to one hub. Normal sign-in is unchanged; these profile records do not introduce test passwords or a passwordless entry path.

Relay destinations appear for eligible non-food baskets at checkout. Existing category, capacity, payment, custody and pickup-proof rules apply. These are fictional sites and logical slots, not physical lockers or real pickup locations. No parcel, paid order or custody evidence was fabricated.
