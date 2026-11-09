# Sequo Demo

**Current setup:** the owner now uses the normal Sequo app with the labelled test catalog in its existing live database. The separate demo app was removed and its services stopped. See [live catalog records and cleanup notes](live-catalog/README.md). The isolated demo instructions below describe the earlier optional setup.

A separate app and API use a fresh PostgreSQL database, independent JWT secrets/issuer/audience and fictional data. Production catalog, accounts, orders and provider settings are not copied.

## Practice

1. Open **Sequo Demo** and enter the private demo access code supplied separately.
2. Browse 12 labelled shops / 120 products. Try clothing sizes/colours, EU shoe sizes, meal options, and 0.5 / 1 / 2 / 5 kg, litre or metre amounts. Some last sizes are intentionally unavailable.
3. Add products, review the cart and select the prefilled **[DEMO] Maison de test** address.
4. Submit the order, then open **Orders**. The normal wallet choices are disconnected; the demo step confirms synthetic payment without contacting either provider.
5. Use one button per step: payment → seller accepts → preparation → ready → choose rider → rider accepts → pickup → delivery. Normal order state and delivery records update after every step.
6. Create another order to repeat. No automatic reset overwrites your practice history.

Use home delivery for the guided demo. Start with one shop; up to four shops can use one of the four test riders per package. Busy riders retain their normal capacity rules. Finish their deliveries to free them again. Guided demo controls simulate actor actions from the customer app; this does not claim a physical handoff or human operation of the Vendor/Rider apps.

Images are deliberately labelled illustrations. They never claim verified camera capture. This dataset contains no real shops, contact numbers, payments or deliveries.

## Build

The demo origin is `https://api.sequoservice.com:8443`. All build-target properties must be supplied together:

```sh
./gradlew :androidApp:assembleDebug -PsequoApiBaseUrl=https://api.sequoservice.com:8443 -PsequoEnvironment=DEMO -PsequoReleaseId=demo-20261109
```

Android demo uses a `.demo` application ID suffix. iOS installation overrides bundle ID to `dev.orestegabo.sequo.Sequo.demo`, app display name to `Sequo Demo`, and disables social-login entitlements for this separate practice build. Use private demo-code sign-in. Ordinary builds still target the live API.

## Graphic source

`catalog/catalog.json` contains names, codes, prices and selling modes. `tools/catalog.py` generates this manifest; `tools/cards.swift` creates deterministic colour-and-text cards using AppKit; `tools/compress.swift` exports 720-pixel JPEGs. These are code-generated graphics, not AI photo assets. The same manifest and final JPEGs are packaged in the backend under `src/main/resources/demo`.

## Verification

Backend regression suite: 774 tests, zero failures/errors (one optional database exercise skipped). Customer demo/auth/selection regression tests: 9 passed. Android APK and signed iOS device build pass. Deployed PostgreSQL journey verification is recorded by the server's private `smoke-result.json`.
