package dev.orestegabo.sequo

import dev.orestegabo.sequo.core.catalog.*
import dev.orestegabo.sequo.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class CatalogSearchTest {
    private val rice = SequoProduct(id = "rice", merchantId = "yawa", name = "Riz parfumé", detail = "Sachet scellé de deux kilos", priceCfa = 1800, label = "Épicerie", optionHint = "")
    private val meal = SequoProduct(id = "meal", merchantId = "mawuli", name = "Riz au poulet", detail = "Repas chaud", priceCfa = 2500, label = "Repas", optionHint = "Sauce au choix")
    private fun shop(name: String, area: String, product: SequoProduct) = SequoShop(name, area, "Commerce", 2.0, "", "", "OPEN", "", "", listOf(product))
    private val yawa = shop("Chez Yawa", "Lomé, Tokoin", rice)
    private val mawuli = shop("Mawuli", "Bè", meal)
    private val riceRecord = SequoProductListingRecord(yawa, rice, "grocery", "Épicerie")
    private val mealRecord = SequoProductListingRecord(mawuli, meal, "food", "Repas")
    private val catalog = CatalogSnapshot(emptyList(), listOf(yawa, mawuli), listOf(riceRecord, mealRecord), emptyList())

    @Test fun namesMatchCaseAccentsAndDecomposedAccents() {
        assertEquals(listOf("rice"), catalog.searchProducts("  RIZ PARFUME  ").map { it.product.id })
        assertEquals(listOf("rice"), catalog.searchProducts("parfume\u0301").map { it.product.id })
    }

    @Test fun multiwordQueryMatchesProductAndActualSellerTogether() {
        val result = catalog.searchProducts("riz tokoin yawa").single()
        assertSame(rice, result.product)
        assertSame(yawa, result.shop)
        assertEquals(1800, result.product.priceCfa)
        assertTrue(catalog.searchProducts("riz mawuli scelle").isEmpty())
    }

    @Test fun descriptionCategoryAreaAndOptionHintsAreSearchable() {
        assertEquals("rice", catalog.searchProducts("scelle epicerie lome").single().product.id)
        assertEquals("meal", catalog.searchProducts("sauce food").single().product.id)
    }

    @Test fun noMatchAndBlankQueriesReturnNoInventedResults() {
        assertTrue(catalog.searchProducts("   ").isEmpty())
        assertTrue(catalog.searchProducts("###").isEmpty())
        assertTrue(catalog.searchProducts("unknown item").isEmpty())
        assertTrue(catalog.copy(products = emptyList()).searchProducts("riz").isEmpty())
    }

    @Test fun serverProductIdentityIsNotCollapsedBySimilarNames() {
        assertEquals(listOf("rice", "meal"), catalog.searchProducts("riz").map { it.product.id })
        val duplicate = catalog.copy(products = catalog.products + riceRecord)
        assertEquals(listOf("rice", "meal"), duplicate.searchProducts("riz").map { it.product.id })
    }

    @Test fun unownedOrMissingIdentityCannotOpenFakeMerchantDetail() {
        val incomplete = catalog.copy(products = listOf(riceRecord.copy(shop = null), mealRecord.copy(product = meal.copy(id = ""))))
        assertTrue(incomplete.searchProducts("riz").isEmpty())
    }
}
