package dev.orestegabo.sequo.ui.markets

import dev.orestegabo.sequo.core.catalog.CatalogSnapshot
import dev.orestegabo.sequo.model.SequoShop
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Storefront
import org.jetbrains.compose.resources.StringResource
import sequo.shared.generated.resources.*

internal enum class CatalogBrowseMode { All, Nearby, Offers, Negotiable }

internal fun CatalogBrowseMode.titleResource(): StringResource = when (this) {
    CatalogBrowseMode.All -> Res.string.browse_all_categories
    CatalogBrowseMode.Nearby -> Res.string.browse_nearby
    CatalogBrowseMode.Offers -> Res.string.browse_offers
    CatalogBrowseMode.Negotiable -> Res.string.browse_negotiable
}

internal fun CatalogBrowseMode.detailResource(): StringResource = when (this) {
    CatalogBrowseMode.All -> Res.string.drawer_intro
    CatalogBrowseMode.Nearby -> Res.string.browse_nearby_detail
    CatalogBrowseMode.Offers -> Res.string.browse_offers_detail
    CatalogBrowseMode.Negotiable -> Res.string.browse_negotiable_detail
}

internal fun CatalogBrowseMode.icon() = when (this) {
    CatalogBrowseMode.All -> Icons.Filled.Storefront
    CatalogBrowseMode.Nearby -> Icons.Filled.NearMe
    CatalogBrowseMode.Offers -> Icons.Filled.LocalOffer
    CatalogBrowseMode.Negotiable -> Icons.Filled.Handshake
}

/** Keep category and shopping-intent filters on the products, even in mixed shops. */
internal fun CatalogSnapshot.browseShops(categoryKey: String, mode: CatalogBrowseMode): List<SequoShop> {
    val categoryIds = categoryKey.takeIf { it.isNotBlank() }?.let { key ->
        products.filter { it.category == key }.map { it.product.id }.toSet()
    }
    val matching = shops.mapNotNull { shop ->
        if (mode == CatalogBrowseMode.Nearby && shop.distanceKm == null) return@mapNotNull null
        val matchingProducts = shop.products.filter { product ->
            (categoryIds == null || product.id in categoryIds) && when (mode) {
                CatalogBrowseMode.Offers -> product.hasDiscount
                CatalogBrowseMode.Negotiable -> product.isNegotiable
                else -> true
            }
        }
        if (matchingProducts.isEmpty()) null else shop.copy(products = matchingProducts)
    }
    return if (mode == CatalogBrowseMode.Nearby) {
        matching.sortedBy { it.distanceKm ?: Double.POSITIVE_INFINITY }
    } else matching
}
