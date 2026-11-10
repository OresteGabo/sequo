package dev.orestegabo.sequo.core.catalog

import dev.orestegabo.sequo.model.SequoProductListing

internal enum class SearchOfferFilter { All, Deals, Negotiable }
internal enum class SearchResultSort { Relevance, PriceAscending, PriceDescending, Nearest }

internal fun List<SequoProductListing>.refineSearchResults(
    query: String, filter: SearchOfferFilter, sort: SearchResultSort,
): List<SequoProductListing> {
    val filtered = filter { listing ->
        when (filter) {
            SearchOfferFilter.All -> true
            SearchOfferFilter.Deals -> listing.product.hasDiscount
            SearchOfferFilter.Negotiable -> listing.product.isNegotiable
        }
    }
    return when (sort) {
        SearchResultSort.Relevance -> filtered.sortedByDescending { it.searchRelevance(query) }
        SearchResultSort.PriceAscending -> filtered.sortedWith(compareBy<SequoProductListing> { !it.product.hasKnownPrice }.thenBy { it.product.priceCfa })
        SearchResultSort.PriceDescending -> filtered.sortedWith(compareBy<SequoProductListing> { !it.product.hasKnownPrice }.thenByDescending { it.product.priceCfa })
        SearchResultSort.Nearest -> filtered.sortedBy { it.searchDistance() ?: Double.POSITIVE_INFINITY }
    }
}

internal fun SequoProductListing.searchDistance(): Double? = shop.distanceKm?.takeIf { it.isFinite() && it >= 0 }

private fun SequoProductListing.searchRelevance(query: String): Int {
    val normalized = query.searchText().trim().replace(Regex("\\s+"), " ")
    if (normalized.isBlank()) return 0
    val terms = normalized.split(' ')
    fun score(text: String): Int {
        val name = text.searchText().trim().replace(Regex("\\s+"), " ")
        return when {
            name == normalized -> 100
            name.startsWith(normalized) -> 70
            terms.all { term -> name.split(' ').any { it.startsWith(term) } } -> 50
            terms.any { it in name } -> 20
            else -> 0
        }
    }
    val reference = product.productReference
    return score(product.name) * 2 + ((listOfNotNull(reference?.name) + reference?.aliases.orEmpty()).maxOfOrNull(::score) ?: 0)
}
