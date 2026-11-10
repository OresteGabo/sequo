package dev.orestegabo.sequo.ui.home

import dev.orestegabo.sequo.ui.components.SequoEmptyState
import dev.orestegabo.sequo.ui.components.EmptyStateVisual
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import dev.orestegabo.sequo.core.catalog.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequo.feature.settings.AppLanguage
import dev.orestegabo.sequo.feature.settings.appText
import sequo.shared.generated.resources.*
import dev.orestegabo.sequo.feature.settings.LocalAppLanguage
import dev.orestegabo.sequo.model.SequoProductListing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CatalogSearchContent(
    query: String, catalog: CatalogSnapshot?, loading: Boolean, failed: Boolean,
    onReload: () -> Unit, onAddProduct: (String) -> Unit,
    onProductSelected: (SequoProductListing) -> Unit,
    onClearQuery: () -> Unit,
    onBrowse: () -> Unit,
) {
    val french = LocalAppLanguage.current == AppLanguage.French
    var filter by remember { mutableStateOf(SearchOfferFilter.All) }
    var sort by remember { mutableStateOf(SearchResultSort.Relevance) }
    var sortMenu by remember { mutableStateOf(false) }
    val rawMatches = remember(catalog, query) { catalog?.searchProducts(query).orEmpty() }
    val hasDistances = rawMatches.any { it.searchDistance() != null }
    val effectiveSort = if (sort == SearchResultSort.Nearest && !hasDistances) SearchResultSort.Relevance else sort
    val matches = remember(rawMatches, query, filter, effectiveSort) { rawMatches.refineSearchResults(query, filter, effectiveSort) }
    if (!loading && !failed && catalog != null && rawMatches.isNotEmpty()) {
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            SearchOfferFilter.entries.forEachIndexed { index, option ->
                SegmentedButton(selected = filter == option, onClick = { filter = option },
                    shape = SegmentedButtonDefaults.itemShape(index, SearchOfferFilter.entries.size), icon = {}) {
                    Text(appText(option.label()), maxLines = 1)
                }
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(appText(if (matches.size == 1) Res.string.search_result_count_one else Res.string.search_result_count).replace("{count}", matches.size.toString()),
                modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Box {
                TextButton(onClick = { sortMenu = true }) {
                    Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(appText(effectiveSort.label()))
                }
                DropdownMenu(expanded = sortMenu, onDismissRequest = { sortMenu = false }) {
                    SearchResultSort.entries.filter { it != SearchResultSort.Nearest || hasDistances }.forEach { option ->
                        DropdownMenuItem(text = { Text(appText(option.label())) },
                            onClick = { sort = option; sortMenu = false },
                            trailingIcon = { if (effectiveSort == option) Text("✓") })
                    }
                }
            }
        }
    }
    when {
        loading -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(Modifier.size(20.dp))
            Text(if (french) "Chargement du catalogue…" else "Loading the catalog…")
        }
        failed || catalog == null -> {
            SearchMessage(if (french) "Catalogue indisponible" else "Catalog unavailable",
                if (french) "Actualisez pour rechercher les produits disponibles." else "Refresh to search available products.")
            OutlinedButton(onClick = onReload) { Text(appText(Res.string.cart_refresh)) }
        }
        catalog.products.isEmpty() -> SequoEmptyState(
            visual = EmptyStateVisual.Catalog,
            title = appText(Res.string.empty_catalog_title), detail = appText(Res.string.empty_catalog_detail),
            actionLabel = appText(Res.string.cart_refresh), onAction = onReload,
        )
        query.isBlank() -> SequoEmptyState(
            visual = EmptyStateVisual.Search,
            compact = true,
            title = appText(Res.string.empty_search_title), detail = appText(Res.string.empty_search_detail),
            actionLabel = appText(Res.string.empty_explore), onAction = onBrowse,
        )
        matches.isEmpty() && rawMatches.isNotEmpty() -> SequoEmptyState(
            visual = EmptyStateVisual.Search, compact = true,
            title = appText(Res.string.search_filtered_empty_title), detail = appText(Res.string.search_filtered_empty_detail),
            actionLabel = appText(Res.string.search_reset_filters), onAction = { filter = SearchOfferFilter.All },
        )
        matches.isEmpty() -> SequoEmptyState(
            visual = EmptyStateVisual.Search,
            compact = true,
            title = appText(Res.string.empty_search_none_title), detail = appText(Res.string.empty_search_none_detail),
            actionLabel = appText(Res.string.empty_clear_search), onAction = onClearQuery,
            secondaryLabel = appText(Res.string.empty_explore), onSecondaryAction = onBrowse,
        )
        else -> {
            // Global price/distance order must not be rearranged by cross-shop family grouping.
            val groups = if (effectiveSort == SearchResultSort.Relevance) matches.sharedProductGroups() else listOf(matches)
            groups.forEach { group ->
                val shops = group.map { it.product.merchantId ?: it.shop.name }.distinct().size
                if (shops > 1 && group.first().product.productReference != null && group.all { it.product.productReference?.id == group.first().product.productReference?.id }) {
                    Text(group.first().product.productReference!!.name + " · " + shops + if (french) " boutiques" else " shops",
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                group.chunked(2).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { listing ->
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Text(listing.shop.name, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                FlashProductCard(
                                    shop = listing.shop, product = listing.product,
                                    onAddProduct = onAddProduct,
                                    onNegotiateClick = { onProductSelected(listing) },
                                    onProductClick = { onProductSelected(listing) },
                                )
                            }
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchMessage(title: String, detail: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = .6f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun SearchOfferFilter.label() = when (this) {
    SearchOfferFilter.All -> Res.string.search_filter_all
    SearchOfferFilter.Deals -> Res.string.search_filter_deals
    SearchOfferFilter.Negotiable -> Res.string.search_filter_negotiable
}

private fun SearchResultSort.label() = when (this) {
    SearchResultSort.Relevance -> Res.string.search_sort_relevance
    SearchResultSort.PriceAscending -> Res.string.search_sort_price_low
    SearchResultSort.PriceDescending -> Res.string.search_sort_price_high
    SearchResultSort.Nearest -> Res.string.search_sort_nearest
}
