package dev.orestegabo.sequo

import dev.orestegabo.sequo.core.catalog.CatalogSnapshot
import dev.orestegabo.sequo.core.catalog.SequoProductListingRecord
import dev.orestegabo.sequo.model.SequoProduct
import dev.orestegabo.sequo.model.SequoShop
import dev.orestegabo.sequo.ui.markets.CatalogBrowseMode
import dev.orestegabo.sequo.ui.markets.browseShops
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CatalogBrowsingTest {
    private fun product(id: String, discount: Boolean = false, negotiable: Boolean = false) = SequoProduct(
        id = id, name = "Same display name", detail = "", priceCfa = 500, label = "", optionHint = "",
        originalPriceCfa = if (discount) 700 else null, isNegotiable = negotiable,
    )

    private fun shop(name: String, distance: Double?, vararg products: SequoProduct) = SequoShop(
        name, null, null, distance, null, null, null, null, null, products.toList(),
    )

    private fun catalog(shops: List<SequoShop>, categories: Map<String, String> = emptyMap()) = CatalogSnapshot(
        emptyList(), shops, shops.flatMap { shop -> shop.products.map {
            SequoProductListingRecord(shop, it, categories[it.id], null)
        } }, emptyList(),
    )

    @Test fun categoryShortcutUsesIdsAndKeepsOtherProductsOutOfMixedShops() {
        val food = product("food")
        val goods = product("goods")
        val snapshot = catalog(listOf(shop("Mixed shop", 1.0, food, goods)), mapOf("food" to "food", "goods" to "electronics"))
        assertEquals(listOf("food"), snapshot.browseShops("food", CatalogBrowseMode.All).single().products.map { it.id })
    }

    @Test fun homeCategorySelectionKeepsMixedShopProductsAndSameNamesSeparate() {
        val food = product("food")
        val goods = product("goods", discount = true)
        val snapshot = catalog(listOf(shop("Mixed", 1.0, food, goods)),
            mapOf("food" to "food", "goods" to "electronics"))
        assertEquals(listOf("food"), snapshot.shopsForCategory("food").single().products.map { it.id })
        assertEquals(listOf("food"), snapshot.featuredProductsFor("food").map { it.second.id })
        assertEquals(listOf("goods"), snapshot.featuredProductsFor("electronics").map { it.second.id })
    }

    @Test fun emptyHomeCategoryDoesNotFallBackToUnrelatedProducts() {
        val snapshot = catalog(listOf(shop("Food shop", 1.0, product("food"))), mapOf("food" to "food"))
        assertTrue(snapshot.shopsForCategory("pharmacy").isEmpty())
        assertTrue(snapshot.featuredProductsFor("pharmacy").isEmpty())
    }

    @Test fun offersShowOnlyDiscountedProductsWithinTheSelectedCategory() {
        val food = product("food", discount = true)
        val goods = product("goods", discount = true)
        val regular = product("regular")
        val snapshot = catalog(listOf(shop("Mixed", 1.0, food, goods), shop("Regular", 2.0, regular)),
            mapOf("food" to "food", "goods" to "electronics", "regular" to "food"))
        assertEquals(listOf("food"), snapshot.browseShops("food", CatalogBrowseMode.Offers).single().products.map { it.id })
        assertEquals(listOf("food", "goods"), snapshot.browseShops("", CatalogBrowseMode.Offers).single().products.map { it.id })
    }

    @Test fun negotiationShortcutUsesProductEligibilityAndCanBeEmpty() {
        val snapshot = catalog(listOf(shop("Shop", null, product("eligible", negotiable = true), product("fixed"))))
        assertEquals(listOf("eligible"), snapshot.browseShops("", CatalogBrowseMode.Negotiable).single().products.map { it.id })
        assertTrue(snapshot.browseShops("missing-category", CatalogBrowseMode.Negotiable).isEmpty())
    }

    @Test fun nearbyShowsOnlyShopsWithDistanceInformationInNearestOrder() {
        val snapshot = catalog(listOf(shop("Unknown", null, product("u")), shop("Far", 5.0, product("f")), shop("Near", 1.0, product("n"))))
        assertEquals(listOf("Near", "Far"), snapshot.browseShops("", CatalogBrowseMode.Nearby).map { it.name })
    }
}
