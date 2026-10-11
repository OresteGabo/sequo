# Sequo Customer MVP Status

Source contract: `/Users/muhirwagabooreste/AndroidStudioProjects/SequoService/sequo-api/docs/contracts/contrat_mvp_sequo.tex`

This file tracks only contract-listed MVP scope for the customer app.

| Contract item | Status | Progress | Evidence | Remaining MVP work |
| --- | --- | ---: | --- | --- |
| Customer login/account creation | Done | 100% | `shared/src/commonMain/kotlin/dev/orestegabo/sequo/core/auth`, `feature/auth` | Keep OAuth config current per app client. |
| Browse products and add items to cart | Done | 90% | `shared/src/commonMain/kotlin/dev/orestegabo/sequo/ui/home`, `ui/catalog`, `ui/basket` | Final catalog data QA with production backend. |
| Multi-product cart | Done | 100% | `shared/src/commonMain/kotlin/dev/orestegabo/sequo/ui/basket` | None identified from contract scope. |
| Customer chooses home delivery or relay point | In progress | 55% | Customer UI plus backend relay/hub domains | Verify checkout path persists chosen fulfillment mode against API. |
| Payment before order validation | In progress | 45% | `PAYMENTS_AND_REFERRALS.md`, basket UI, API payment domain | Finish provider states and block completion until validated. |
| Customer order tracking | In progress | 80% | `shared/src/commonMain/kotlin/dev/orestegabo/sequo/ui/orders` | Live tracking quality depends on rider/admin integration. |
| Customer return request within allowed delay | In progress | 45% | `RETURNS_POLICY.md`, order labels, API returns domain | Complete return request UI and endpoint wiring. |
| Customer can negotiate when allowed | In progress | 45% | `BARGAINING_FLOW.md`, product negotiation hooks | Finish submit/status UX and seller response integration. |
| Referral delivery credit, not cash | In progress | 60% | `PAYMENTS_AND_REFERRALS.md`, API referral domain | Surface ledger and checkout application clearly. |

Recommended verification:

```bash
./gradlew :androidApp:compileDebugKotlin
```
