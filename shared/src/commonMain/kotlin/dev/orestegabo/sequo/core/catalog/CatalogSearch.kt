package dev.orestegabo.sequo.core.catalog

import dev.orestegabo.sequo.model.SequoProductListing

/** Searches only the current server catalog and retains its product and seller identity. */
internal fun CatalogSnapshot.searchProducts(query: String): List<SequoProductListing> {
    val terms = query.searchText().split(' ').filter(String::isNotBlank)
    if (terms.isEmpty()) return emptyList()
    return products.mapNotNull { record ->
        val shop = record.shop ?: return@mapNotNull null
        if (record.product.id.isBlank()) return@mapNotNull null
        val category = categories.firstOrNull { it.key == record.category }
        val searchable = listOfNotNull(
            record.product.name, record.product.detail, record.product.label,
            record.product.productReference?.name, record.product.productReference?.aliases?.joinToString(" "),
            record.product.optionHint, record.subcategory, record.category,
            category?.title, shop.name, shop.area, shop.kind,
        ).joinToString(" ").searchText()
        if (terms.all(searchable::contains)) SequoProductListing(shop, record.product) else null
    }.distinctBy { it.product.id }
}

internal fun String.searchText(): String = buildString {
    this@searchText.lowercase().forEach { character ->
        when (character) {
            'à', 'â', 'ä', 'á', 'ã', 'å' -> append('a')
            'ç' -> append('c')
            'é', 'è', 'ê', 'ë' -> append('e')
            'î', 'ï', 'í', 'ì' -> append('i')
            'ô', 'ö', 'ó', 'ò', 'õ' -> append('o')
            'ù', 'û', 'ü', 'ú' -> append('u')
            'ÿ', 'ý' -> append('y')
            'œ' -> append("oe")
            'æ' -> append("ae")
            in '\u0300'..'\u036f' -> Unit
            else -> append(if (character.isLetterOrDigit()) character else ' ')
        }
    }
}

/** Group related offers for browsing; preserve every shop's purchasable listing ID. */
internal fun List<SequoProductListing>.sharedProductGroups(): List<List<SequoProductListing>> =
    groupBy { it.product.productReference?.id }.let { groups ->
        val shared = groups.filter { (id, offers) -> id != null && offers.map { it.product.merchantId ?: it.shop.name }.distinct().size > 1 }.keys
        groupBy { it.product.productReference?.id?.takeIf(shared::contains) }.values.toList()
    }
