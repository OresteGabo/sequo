package dev.orestegabo.sequo

import dev.orestegabo.sequo.core.catalog.*
import dev.orestegabo.sequo.model.*
import kotlin.test.*

class SearchResultOptionsTest {
    private fun offer(id: String, price: Int, distance: Double? = null, discount: Boolean = false, negotiable: Boolean = false,
        name: String = id, knownPrice: Boolean = true, reference: SharedProductReference? = null) =
        SequoProductListing(SequoShop(id, "Lomé", "Shop", distance, null, null, null, null, null, emptyList()),
            SequoProduct(id = id, merchantId = id, name = name, detail = "", priceCfa = price, label = "", optionHint = "",
                originalPriceCfa = if (discount) price + 200 else null, isNegotiable = negotiable,
                hasKnownPrice = knownPrice, productReference = reference))
    private fun List<SequoProductListing>.ids() = map { it.product.id }

    @Test fun filtersUseRealOfferFlagsAndPreserveShopIdentity() {
        val offers = listOf(offer("normal", 900), offer("deal", 1000, discount = true), offer("bargain", 1200, negotiable = true))
        val deal = offers.refineSearchResults("", SearchOfferFilter.Deals, SearchResultSort.Relevance).single()
        assertSame(offers[1], deal)
        assertEquals(listOf("bargain"), offers.refineSearchResults("", SearchOfferFilter.Negotiable, SearchResultSort.Relevance).ids())
        assertEquals(3, offers.refineSearchResults("", SearchOfferFilter.All, SearchResultSort.Relevance).size)
        assertTrue(listOf(offers[0]).refineSearchResults("", SearchOfferFilter.Deals, SearchResultSort.Relevance).isEmpty())
    }

    @Test fun priceSortKeepsUnknownPricesLastAndGenuineZeroPricesValid() {
        val offers = listOf(offer("unknown", 0, knownPrice = false), offer("high", 2000), offer("free", 0), offer("low", 500))
        assertEquals(listOf("free", "low", "high", "unknown"), offers.refineSearchResults("", SearchOfferFilter.All, SearchResultSort.PriceAscending).ids())
        assertEquals(listOf("high", "low", "free", "unknown"), offers.refineSearchResults("", SearchOfferFilter.All, SearchResultSort.PriceDescending).ids())
    }

    @Test fun nearestUsesOnlyValidKnownDistances() {
        val offers = listOf(offer("unknown", 100), offer("far", 100, 8.0), offer("near", 100, 0.5), offer("invalid", 100, -1.0), offer("nan", 100, Double.NaN))
        assertEquals(listOf("near", "far", "unknown", "invalid", "nan"), offers.refineSearchResults("", SearchOfferFilter.All, SearchResultSort.Nearest).ids())
    }

    @Test fun relevancePrefersProductAndSharedAliasesOverIncidentalMatches() {
        val reference = SharedProductReference("milk", "Lait", "grocery", listOf("milk"))
        val offers = listOf(offer("incidental", 100, name = "Chocolate"), offer("linked", 100, name = "Brand A 1 L", reference = reference), offer("exact", 100, name = "Lait"))
        assertEquals(listOf("exact", "linked", "incidental"), offers.refineSearchResults("LAIT", SearchOfferFilter.All, SearchResultSort.Relevance).ids())
        assertEquals("linked", offers.refineSearchResults("milk", SearchOfferFilter.All, SearchResultSort.Relevance).first().product.id)
    }
}
