package dev.orestegabo.sequo.ui.app

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequo.data.*
import dev.orestegabo.sequo.domain.*
import dev.orestegabo.sequo.logic.*
import dev.orestegabo.sequo.model.*
import dev.orestegabo.sequo.theme.*
import dev.orestegabo.sequo.ui.account.*
import dev.orestegabo.sequo.ui.app.*
import dev.orestegabo.sequo.ui.basket.*
import dev.orestegabo.sequo.ui.catalog.*
import dev.orestegabo.sequo.ui.chrome.*
import dev.orestegabo.sequo.ui.components.*
import dev.orestegabo.sequo.ui.home.*
import dev.orestegabo.sequo.ui.markets.*
import dev.orestegabo.sequo.ui.orders.*
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import sequo.shared.generated.resources.*

@Composable
internal fun SequoShell() {
    var currentDestination by remember { mutableStateOf(SequoSection.Home) }
    var extraBasketItems by remember { mutableStateOf(0) }
    var searchVisible by remember { mutableStateOf(false) }
    var selectedMarketTypeKey by remember { mutableStateOf(sequoShopTypes.first().key) }
    var selectedProductListing by remember { mutableStateOf<SequoProductListing?>(null) }
    val categoryUsage = remember { mutableStateMapOf<String, Int>() }
    val pinnedCategoryKeys = remember { mutableStateListOf("food") }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val basketCount = sequoBasket.sumOf { it.quantity } + extraBasketItems

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SequoNavigationDrawer(
                selectedMarketTypeKey = selectedMarketTypeKey,
                onMarketTypeSelected = { typeKey ->
                    categoryUsage[typeKey] = (categoryUsage[typeKey] ?: 0) + 1
                    selectedMarketTypeKey = typeKey
                    currentDestination = SequoSection.Markets
                    searchVisible = false
                    scope.launch { drawerState.close() }
                },
            )
        },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            SequoAmbientBackground(modifier = Modifier.fillMaxSize())
            SequoContentStage(
                currentDestination = currentDestination,
                onDestinationSelected = { currentDestination = it },
                selectedProductListing = selectedProductListing,
                onProductSelected = { listing ->
                    selectedProductListing = listing
                    searchVisible = false
                },
                onCloseProduct = { selectedProductListing = null },
                extraBasketItems = extraBasketItems,
                searchVisible = searchVisible,
                onCloseSearch = { searchVisible = false },
                selectedMarketTypeKey = selectedMarketTypeKey,
                categoryUsage = categoryUsage,
                pinnedCategoryKeys = pinnedCategoryKeys,
                onCategoryUsed = { typeKey ->
                    categoryUsage[typeKey] = (categoryUsage[typeKey] ?: 0) + 1
                },
                onToggleCategoryPinned = { typeKey ->
                    if (typeKey in pinnedCategoryKeys) {
                        pinnedCategoryKeys.remove(typeKey)
                    } else if (pinnedCategoryKeys.size < 3) {
                        pinnedCategoryKeys.add(typeKey)
                    }
                },
                onAddProduct = { extraBasketItems += 1 },
                modifier = Modifier.fillMaxSize(),
            )
            SequoTopAppBar(
                currentDestination = currentDestination,
                onMenuClick = { scope.launch { drawerState.open() } },
                onSearchClick = { searchVisible = !searchVisible },
                onNotificationsClick = { currentDestination = SequoSection.Orders },
                productListing = selectedProductListing,
                onBackClick = {
                    selectedProductListing = null
                    searchVisible = false
                },
                modifier = Modifier.align(Alignment.TopCenter),
            )
            SequoBottomBar(
                currentDestination = currentDestination,
                onDestinationSelected = {
                    currentDestination = it
                    searchVisible = false
                    selectedProductListing = null
                },
                pendingBasketCount = basketCount,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
internal fun SequoContentStage(
    currentDestination: SequoSection,
    onDestinationSelected: (SequoSection) -> Unit,
    selectedProductListing: SequoProductListing?,
    onProductSelected: (SequoProductListing) -> Unit,
    onCloseProduct: () -> Unit,
    extraBasketItems: Int,
    searchVisible: Boolean,
    onCloseSearch: () -> Unit,
    selectedMarketTypeKey: String,
    categoryUsage: Map<String, Int>,
    pinnedCategoryKeys: List<String>,
    onCategoryUsed: (String) -> Unit,
    onToggleCategoryPinned: (String) -> Unit,
    onAddProduct: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (selectedProductListing != null) {
        ProductDetailScreenColumn(modifier = modifier) {
            ProductDetailContent(
                listing = selectedProductListing,
                onAddProduct = onAddProduct,
                onProductSelected = onProductSelected,
                onNegotiateClick = {},
            )
        }
    } else {
        SequoScreenColumn(modifier = modifier) {
            if (searchVisible) {
                SequoSearchCard(onClose = onCloseSearch)
            }
            when (currentDestination) {
                SequoSection.Home -> HomeContent(
                    onDestinationSelected = onDestinationSelected,
                    onAddProduct = onAddProduct,
                    onProductSelected = onProductSelected,
                    categoryUsage = categoryUsage,
                    pinnedCategoryKeys = pinnedCategoryKeys,
                    onCategoryUsed = onCategoryUsed,
                    onToggleCategoryPinned = onToggleCategoryPinned,
                )
                SequoSection.Markets -> MarketsContent(
                    selectedTypeKey = selectedMarketTypeKey,
                    onAddProduct = onAddProduct,
                    onProductSelected = onProductSelected,
                )
                SequoSection.Basket -> BasketContent(extraBasketItems)
                SequoSection.Orders -> OrdersContent()
                SequoSection.Account -> AccountContent()
            }
        }
    }
}

@Composable
internal fun SequoScreenColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, top = 104.dp, end = 20.dp, bottom = 126.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content,
    )
}

@Composable
internal fun ProductDetailScreenColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 126.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        content = content,
    )
}

@Composable
private fun SequoNavigationDrawer(
    selectedMarketTypeKey: String,
    onMarketTypeSelected: (String) -> Unit,
) {
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.width(316.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 14.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SequoIconMark(Icons.Filled.Storefront, MaterialTheme.colorScheme.primary, Modifier.size(40.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Sequo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    Text("Browse faster", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.58f))
            Text(
                "Shop categories",
                modifier = Modifier.padding(start = 12.dp, top = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
            )
            sequoShopTypes.forEach { type ->
                NavigationDrawerItem(
                    label = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(type.title, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            when (type.key) {
                                "food" -> Text("Now", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    },
                    selected = selectedMarketTypeKey == type.key,
                    onClick = { onMarketTypeSelected(type.key) },
                    icon = { Icon(type.icon, contentDescription = null, tint = type.accent) },
                    shape = RoundedCornerShape(14.dp),
                )
            }
            Text(
                "Marketplace tools",
                modifier = Modifier.padding(start = 12.dp, top = 10.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
            )
            DrawerToolRow(Icons.Filled.LocalOffer, "Promos & campaigns", "Lunch, holidays, weekend deals")
            DrawerToolRow(Icons.Filled.FavoriteBorder, "Saved shops", "Favorite sellers and repeat buys")
            DrawerToolRow(Icons.Filled.Place, "Delivery areas", "Lome zones and fees")
            DrawerToolRow(Icons.Filled.SupportAgent, "Support", "Orders, refunds, seller help")
        }
    }
}

@Composable
private fun DrawerToolRow(
    icon: ImageVector,
    title: String,
    detail: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.56f))
            .padding(horizontal = 14.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(21.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(detail, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
