package dev.orestegabo.sequo.core.catalog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import dev.orestegabo.sequo.core.network.createSequoHttpClient
import dev.orestegabo.sequo.model.SequoProduct
import dev.orestegabo.sequo.model.SequoPromo
import dev.orestegabo.sequo.model.SequoShop
import dev.orestegabo.sequo.model.SequoShopType
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.Serializable

internal class CatalogApiClient(
    private val httpClient: HttpClient = createSequoHttpClient(),
) {
    suspend fun home(category: String? = null, subcategory: String? = null): CatalogSnapshot {
        val response = httpClient.get("/api/catalog/home") {
            url {
                category?.let { parameters.append("category", it) }
                subcategory?.let { parameters.append("subcategory", it) }
            }
        }.body<CommerceHomeResponse>()

        return response.toCatalogSnapshot()
    }
}

internal data class CatalogSnapshot(
    val categories: List<SequoShopType>,
    val shops: List<SequoShop>,
    val products: List<SequoProductListingRecord>,
    val promotions: List<SequoPromo>,
) {
    val defaultCategoryKey: String = categories.firstOrNull()?.key.orEmpty()

    fun categoryFor(key: String): SequoShopType =
        categories.firstOrNull { it.key == key } ?: categories.firstOrNull() ?: fallbackCategory

    fun shopsForCategory(key: String): List<SequoShop> {
        if (key.isBlank()) return shops
        val categoryProducts = products.filter { it.category == key }.map { it.product.name }.toSet()
        return shops.filter { shop -> shop.products.any { it.name in categoryProducts } }
    }

    fun featuredProductsFor(categoryKey: String): List<Pair<SequoShop, SequoProduct>> {
        val sourceShops = shopsForCategory(categoryKey).ifEmpty { shops }
        return sourceShops
            .flatMap { shop -> shop.products.map { product -> shop to product } }
            .sortedWith(
                compareByDescending<Pair<SequoShop, SequoProduct>> { it.second.hasDiscount }
                    .thenByDescending { it.second.isNegotiable }
                    .thenBy { it.first.distanceKm },
            )
            .take(8)
    }
}

internal data class SequoProductListingRecord(
    val shop: SequoShop?,
    val product: SequoProduct,
    val category: String?,
    val subcategory: String?,
)

@Serializable
private data class CommerceHomeResponse(
    val categories: List<CatalogCategoryResponse> = emptyList(),
    val merchants: List<MerchantStorefrontResponse> = emptyList(),
    val products: List<CatalogProductResponse> = emptyList(),
    val promotions: List<CatalogPromotionResponse> = emptyList(),
)

@Serializable
private data class CatalogCategoryResponse(
    val key: String,
    val title: String,
    val supportLabel: String,
    val accentHex: String,
)

@Serializable
private data class MerchantStorefrontResponse(
    val id: String,
    val name: String,
    val status: String,
    val area: String? = null,
    val kind: String? = null,
    val distanceKm: Double? = null,
    val eta: String? = null,
    val photoStatus: String? = null,
    val openStatus: String? = null,
    val rating: String? = null,
    val consolidation: String? = null,
    val products: List<CatalogProductResponse> = emptyList(),
)

@Serializable
private data class CatalogProductResponse(
    val id: String,
    val merchantId: String? = null,
    val name: String,
    val category: String? = null,
    val subcategory: String? = null,
    val detail: String? = null,
    val priceCfa: Int? = null,
    val optionHint: String? = null,
    val originalPriceCfa: Int? = null,
    val bargainingEnabled: Boolean = false,
    val cameraVerified: Boolean = false,
    val capturedAtLabel: String? = null,
)

@Serializable
private data class CatalogPromotionResponse(
    val id: String,
    val headline: String,
    val title: String,
    val subtitle: String,
    val productId: String,
)

private fun CommerceHomeResponse.toCatalogSnapshot(): CatalogSnapshot {
    val mappedCategories = categories.map { it.toShopType() }
    val mappedShops = merchants.map { it.toShop() }
    val shopsByProductId = mappedShops
        .flatMap { shop -> shop.products.mapNotNull { product -> product.id.takeIf { it.isNotBlank() }?.let { it to shop } } }
        .toMap()
    val listings = products.map { product ->
        val mappedProduct = product.toProduct()
        SequoProductListingRecord(
            shop = shopsByProductId[mappedProduct.id],
            product = mappedProduct,
            category = product.category,
            subcategory = product.subcategory,
        )
    }
    val promotionsByProductId = products.associateBy { it.id }
    val promotionsByProductIdListing = listings.associateBy { it.product.id }
    val mappedPromotions = promotions.mapNotNull { promo ->
        val productResponse = promotionsByProductId[promo.productId] ?: return@mapNotNull null
        val listing = promotionsByProductIdListing[productResponse.id] ?: return@mapNotNull null
        val shop = listing.shop ?: return@mapNotNull null
        SequoPromo(
            headline = promo.headline,
            title = promo.title,
            subtitle = promo.subtitle,
            shop = shop,
            product = listing.product,
        )
    }
    return CatalogSnapshot(
        categories = mappedCategories.ifEmpty { listOf(fallbackCategory) },
        shops = mappedShops,
        products = listings,
        promotions = mappedPromotions,
    )
}

private fun CatalogCategoryResponse.toShopType(): SequoShopType =
    SequoShopType(
        key = key,
        title = title,
        supportLabel = supportLabel,
        icon = iconForCategory(key),
        accent = accentHex.toColorOrDefault(),
    )

private fun MerchantStorefrontResponse.toShop(): SequoShop =
    SequoShop(
        name = name,
        area = area.orEmpty(),
        kind = kind ?: status,
        distanceKm = distanceKm ?: 0.0,
        eta = eta ?: "Available",
        photoStatus = photoStatus ?: "Catalog synced",
        openStatus = openStatus ?: status.lowercase().replaceFirstChar { it.uppercase() },
        rating = rating ?: "-",
        consolidation = consolidation ?: "Standard delivery",
        products = products.map { it.toProduct() },
    )

private fun CatalogProductResponse.toProduct(): SequoProduct =
    SequoProduct(
        id = id,
        merchantId = merchantId,
        name = name,
        detail = detail.orEmpty(),
        priceCfa = priceCfa ?: 0,
        label = subcategory ?: category ?: "Catalog",
        optionHint = optionHint.orEmpty(),
        bargainNote = if (bargainingEnabled) "Negotiable" else null,
        subcategory = subcategory.orEmpty(),
        originalPriceCfa = originalPriceCfa,
        isCameraVerified = cameraVerified,
        capturedAtLabel = capturedAtLabel,
    )

private val fallbackCategory = SequoShopType(
    key = "all",
    title = "All",
    supportLabel = "Catalog",
    icon = Icons.Filled.Storefront,
    accent = Color(0xFF286A46),
)

private fun iconForCategory(key: String): ImageVector =
    when (key.lowercase()) {
        "food" -> Icons.Filled.LocalDining
        "grocery" -> Icons.Filled.LocalGroceryStore
        "fashion" -> Icons.Filled.Checkroom
        "electronics" -> Icons.Filled.Devices
        "pharmacy" -> Icons.Filled.LocalPharmacy
        "beauty" -> Icons.Filled.LocalFlorist
        "bargains" -> Icons.Filled.LocalOffer
        else -> Icons.Filled.Storefront
    }

private fun String.toColorOrDefault(): Color {
    val sanitized = removePrefix("#")
    return runCatching { Color(sanitized.toLong(16) or 0xFF000000) }
        .getOrDefault(Color(0xFF286A46))
}
