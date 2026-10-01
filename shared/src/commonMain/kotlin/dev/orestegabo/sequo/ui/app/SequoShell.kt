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
import dev.orestegabo.sequo.core.auth.CurrentUser
import dev.orestegabo.sequo.core.catalog.CatalogApiClient
import dev.orestegabo.sequo.core.catalog.CatalogSnapshot
import dev.orestegabo.sequo.feature.legal.LegalInitialTab
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
import dev.orestegabo.sequo.ui.notifications.*
import dev.orestegabo.sequo.ui.orders.*
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import sequo.shared.generated.resources.*

@Composable
internal fun SequoShell(
    currentUser: CurrentUser?,
    isGuest: Boolean,
    onOpenLegal: (LegalInitialTab) -> Unit,
    onHomeEntered: () -> Unit,
    openNotificationsRequest: Int,
    onSignInRequested: () -> Unit,
    onLogout: () -> Unit,
) {
    var currentDestination by remember { mutableStateOf(SequoSection.Home) }
    var notificationBackDestination by remember { mutableStateOf<SequoSection?>(null) }
    var extraBasketItems by remember { mutableStateOf(0) }
    var searchVisible by remember { mutableStateOf(false) }
    var selectedMarketTypeKey by remember { mutableStateOf("") }
    var selectedProductListing by remember { mutableStateOf<SequoProductListing?>(null) }
    var catalogState by remember { mutableStateOf<CatalogUiState>(CatalogUiState.Loading) }
    var notificationUnreadCount by remember { mutableStateOf(defaultNotificationUnreadCount()) }
    val catalogClient = remember { CatalogApiClient() }
    val categoryUsage = remember { mutableStateMapOf<String, Int>() }
    val pinnedCategoryKeys = remember { mutableStateListOf("food") }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val basketCount = extraBasketItems

    fun reloadCatalog() {
        scope.launch {
            catalogState = CatalogUiState.Loading
            catalogState = runCatching { catalogClient.home() }
                .fold(
                    onSuccess = { snapshot ->
                        if (selectedMarketTypeKey.isBlank()) selectedMarketTypeKey = snapshot.defaultCategoryKey
                        CatalogUiState.Ready(snapshot)
                    },
                    onFailure = { CatalogUiState.Failed(it.message ?: "Unable to load Sequo catalog.") },
                )
        }
    }

    LaunchedEffect(Unit) {
        reloadCatalog()
    }

    LaunchedEffect(currentDestination, selectedProductListing) {
        if (currentDestination == SequoSection.Home && selectedProductListing == null) {
            onHomeEntered()
        }
    }

    LaunchedEffect(openNotificationsRequest) {
        if (openNotificationsRequest > 0) {
            notificationBackDestination = SequoSection.Home
            currentDestination = SequoSection.Notifications
            searchVisible = false
            selectedProductListing = null
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SequoNavigationDrawer(
                currentUser = currentUser,
                isGuest = isGuest,
                selectedMarketTypeKey = selectedMarketTypeKey,
                shopTypes = (catalogState as? CatalogUiState.Ready)?.snapshot?.categories.orEmpty(),
                onMarketTypeSelected = { typeKey ->
                    categoryUsage[typeKey] = (categoryUsage[typeKey] ?: 0) + 1
                    selectedMarketTypeKey = typeKey
                    currentDestination = SequoSection.Markets
                    notificationBackDestination = null
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
                onDestinationSelected = {
                    currentDestination = it
                    notificationBackDestination = null
                },
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
                catalogState = catalogState,
                onReloadCatalog = ::reloadCatalog,
                currentUser = currentUser,
                isGuest = isGuest,
                onNotificationUnreadCountChanged = { notificationUnreadCount = it },
                onOpenLegal = onOpenLegal,
                onSignInRequested = onSignInRequested,
                onLogout = onLogout,
                modifier = Modifier.fillMaxSize(),
            )
            SequoTopAppBar(
                currentDestination = currentDestination,
                onMenuClick = { scope.launch { drawerState.open() } },
                onSearchClick = { searchVisible = !searchVisible },
                onNotificationsClick = {
                    notificationBackDestination = if (currentDestination == SequoSection.Notifications) {
                        SequoSection.Home
                    } else {
                        currentDestination
                    }
                    currentDestination = SequoSection.Notifications
                    searchVisible = false
                    selectedProductListing = null
                },
                notificationUnreadCount = notificationUnreadCount,
                productListing = selectedProductListing,
                showBackButton = notificationBackDestination != null && currentDestination == SequoSection.Notifications,
                onBackClick = {
                    val notificationReturn = notificationBackDestination
                    if (notificationReturn != null && currentDestination == SequoSection.Notifications) {
                        currentDestination = notificationReturn
                        notificationBackDestination = null
                    }
                    selectedProductListing = null
                    searchVisible = false
                },
                modifier = Modifier.align(Alignment.TopCenter),
            )
            SequoBottomBar(
                currentDestination = currentDestination,
                onDestinationSelected = {
                    currentDestination = it
                    notificationBackDestination = null
                    searchVisible = false
                    selectedProductListing = null
                },
                pendingBasketCount = basketCount,
                notificationUnreadCount = notificationUnreadCount,
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
    catalogState: CatalogUiState,
    onReloadCatalog: () -> Unit,
    currentUser: CurrentUser?,
    isGuest: Boolean,
    onNotificationUnreadCountChanged: (Int) -> Unit,
    onOpenLegal: (LegalInitialTab) -> Unit,
    onSignInRequested: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val catalog = (catalogState as? CatalogUiState.Ready)?.snapshot
    if (catalogState is CatalogUiState.Loading) {
        SequoScreenColumn(modifier = modifier) {
            SequoCatalogLoadingCard()
        }
    } else if (catalogState is CatalogUiState.Failed) {
        SequoScreenColumn(modifier = modifier) {
            SequoErrorPanel(
                kind = SequoErrorKind.Server,
                onRetry = onReloadCatalog,
                technicalNote = catalogState.message,
            )
        }
    } else if (selectedProductListing != null && catalog != null) {
        ProductDetailScreenColumn(modifier = modifier) {
            ProductDetailContent(
                listing = selectedProductListing,
                allShops = catalog.shops,
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
                    catalog = requireNotNull(catalog),
                    onDestinationSelected = onDestinationSelected,
                    onAddProduct = onAddProduct,
                    onProductSelected = onProductSelected,
                    categoryUsage = categoryUsage,
                    pinnedCategoryKeys = pinnedCategoryKeys,
                    onCategoryUsed = onCategoryUsed,
                    onToggleCategoryPinned = onToggleCategoryPinned,
                )
                SequoSection.Markets -> MarketsContent(
                    catalog = requireNotNull(catalog),
                    selectedTypeKey = selectedMarketTypeKey,
                    onAddProduct = onAddProduct,
                    onProductSelected = onProductSelected,
                )
                SequoSection.Basket -> if (isGuest) {
                    GuestSignInPanel(
                        title = "Sign in to build your basket",
                        detail = "You can inspect products and menus first. Saving cart items, choosing an address, and checkout require an account.",
                        onSignIn = onSignInRequested,
                        onBrowse = { onDestinationSelected(SequoSection.Markets) },
                    )
                } else {
                    BasketContent(extraBasketItems)
                }
                SequoSection.Orders -> if (isGuest) {
                    GuestSignInPanel(
                        title = "Sign in to track orders",
                        detail = "You can browse products as a guest. Orders, delivery status, pickup codes, returns, and receipts stay behind your account.",
                        onSignIn = onSignInRequested,
                        onBrowse = { onDestinationSelected(SequoSection.Home) },
                    )
                } else {
                    OrdersContent()
                }
                SequoSection.Notifications -> NotificationsContent(
                    onOpenOrders = { onDestinationSelected(SequoSection.Orders) },
                    onOpenPrivacy = { onOpenLegal(LegalInitialTab.Privacy) },
                    onOpenTerms = { onOpenLegal(LegalInitialTab.Terms) },
                    onUnreadCountChanged = onNotificationUnreadCountChanged,
                )
                SequoSection.Account -> AccountContent(
                    currentUser = currentUser,
                    isGuest = isGuest,
                    onSignInRequested = onSignInRequested,
                    onLogout = onLogout,
                )
            }
        }
    }
}

@Composable
private fun GuestSignInPanel(
    title: String,
    detail: String,
    onSignIn: () -> Unit,
    onBrowse: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.58f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.58f)),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(14.dp),
                )
            }
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                detail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = onBrowse,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text("Browse")
                }
                Button(
                    onClick = onSignIn,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text("Sign in")
                }
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
    currentUser: CurrentUser?,
    isGuest: Boolean,
    selectedMarketTypeKey: String,
    shopTypes: List<SequoShopType>,
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
                SequoShoppingMark(Modifier.size(40.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        currentUser?.displayName ?: "Sequo customer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        when {
                            isGuest -> "Guest browsing"
                            currentUser?.email != null -> currentUser.email
                            else -> "Signed in"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
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
            if (shopTypes.isEmpty()) {
                Text(
                    "Catalog loading",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            shopTypes.forEach { type ->
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

internal sealed interface CatalogUiState {
    data object Loading : CatalogUiState
    data class Ready(val snapshot: CatalogSnapshot) : CatalogUiState
    data class Failed(val message: String) : CatalogUiState
}

@Composable
private fun SequoCatalogLoadingCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.50f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.58f)),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Loading Sequo catalog", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Products and shops are being fetched from the API.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
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
