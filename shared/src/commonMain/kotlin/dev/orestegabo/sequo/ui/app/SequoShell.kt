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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequo.core.auth.CurrentUser
import dev.orestegabo.sequo.core.catalog.CatalogApiClient
import dev.orestegabo.sequo.core.catalog.CatalogSnapshot
import dev.orestegabo.sequo.feature.legal.LegalInitialTab
import dev.orestegabo.sequo.feature.settings.AppLanguage
import dev.orestegabo.sequo.feature.settings.AppThemePreference
import dev.orestegabo.sequo.feature.settings.appCatalogText
import dev.orestegabo.sequo.feature.settings.appText
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
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    themePreference: AppThemePreference,
    onThemePreferenceChange: (AppThemePreference) -> Unit,
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
    val catalogLoadError = appText(Res.string.home_catalog_load_error)

    fun reloadCatalog() {
        scope.launch {
            catalogState = CatalogUiState.Loading
            catalogState = runCatching { catalogClient.home() }
                .fold(
                    onSuccess = { snapshot ->
                        if (selectedMarketTypeKey.isBlank()) selectedMarketTypeKey = snapshot.defaultCategoryKey
                        CatalogUiState.Ready(snapshot)
                    },
                    onFailure = { CatalogUiState.Failed(catalogLoadError) },
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
                language = language,
                onLanguageChange = onLanguageChange,
                themePreference = themePreference,
                onThemePreferenceChange = onThemePreferenceChange,
                selectedMarketTypeKey = selectedMarketTypeKey,
                shopTypes = (catalogState as? CatalogUiState.Ready)?.snapshot?.categories.orEmpty(),
                onDestinationSelected = { destination ->
                    currentDestination = destination
                    notificationBackDestination = null
                    searchVisible = false
                    selectedProductListing = null
                    scope.launch { drawerState.close() }
                },
                onMarketTypeSelected = { typeKey ->
                    categoryUsage[typeKey] = (categoryUsage[typeKey] ?: 0) + 1
                    selectedMarketTypeKey = typeKey
                    currentDestination = SequoSection.Markets
                    notificationBackDestination = null
                    searchVisible = false
                    selectedProductListing = null
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
            SequoCatalogLoadingSkeleton()
        }
    } else if (catalogState is CatalogUiState.Failed) {
        SequoScreenColumn(modifier = modifier) {
            SequoErrorPanel(
                kind = SequoErrorKind.Server,
                onRetry = onReloadCatalog,
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
                        title = appText(Res.string.guest_basket_title),
                        detail = appText(Res.string.guest_basket_detail),
                        onSignIn = onSignInRequested,
                        onBrowse = { onDestinationSelected(SequoSection.Markets) },
                    )
                } else {
                    BasketContent(extraBasketItems)
                }
                SequoSection.Orders -> if (isGuest) {
                    GuestSignInPanel(
                        title = appText(Res.string.guest_orders_title),
                        detail = appText(Res.string.guest_orders_detail),
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
                    Text(appText(Res.string.common_browse))
                }
                Button(
                    onClick = onSignIn,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(appText(Res.string.auth_dialog_title))
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
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    themePreference: AppThemePreference,
    onThemePreferenceChange: (AppThemePreference) -> Unit,
    selectedMarketTypeKey: String,
    shopTypes: List<SequoShopType>,
    onDestinationSelected: (SequoSection) -> Unit,
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
                .verticalScroll(rememberScrollState())
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
                        currentUser?.displayName ?: appText(Res.string.drawer_customer),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        when {
                            isGuest -> appText(Res.string.drawer_guest)
                            currentUser?.email != null -> currentUser.email
                            else -> appText(Res.string.drawer_signed_in)
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
                appText(Res.string.drawer_quick_links),
                modifier = Modifier.padding(start = 12.dp, top = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
            )
            DrawerQuickLinkGrid(onDestinationSelected = onDestinationSelected)
            Text(
                appText(Res.string.drawer_settings),
                modifier = Modifier.padding(start = 12.dp, top = 10.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
            )
            DrawerSettingsPanel(
                language = language,
                onLanguageChange = onLanguageChange,
                themePreference = themePreference,
                onThemePreferenceChange = onThemePreferenceChange,
            )
            HorizontalDivider(
                modifier = Modifier.padding(top = 6.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.58f),
            )
            Text(
                appText(Res.string.drawer_shop_categories),
                modifier = Modifier.padding(start = 12.dp, top = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
            )
            if (shopTypes.isEmpty()) {
                Text(
                    appText(Res.string.drawer_catalog_loading),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            shopTypes.forEach { type ->
                NavigationDrawerItem(
                    label = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(appCatalogText(type.title), modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            when (type.key) {
                                "food" -> Text(appText(Res.string.drawer_now), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
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
                appText(Res.string.drawer_marketplace_tools),
                modifier = Modifier.padding(start = 12.dp, top = 10.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
            )
            DrawerToolRow(Icons.Filled.LocalOffer, appText(Res.string.drawer_promos), appText(Res.string.drawer_promos_detail))
            DrawerToolRow(Icons.Filled.FavoriteBorder, appText(Res.string.drawer_saved_shops), appText(Res.string.drawer_saved_shops_detail))
            DrawerToolRow(Icons.Filled.Place, appText(Res.string.drawer_delivery_areas), appText(Res.string.drawer_delivery_areas_detail))
            DrawerToolRow(Icons.Filled.SupportAgent, appText(Res.string.drawer_support), appText(Res.string.drawer_support_detail))
        }
    }
}

internal sealed interface CatalogUiState {
    data object Loading : CatalogUiState
    data class Ready(val snapshot: CatalogSnapshot) : CatalogUiState
    data class Failed(val message: String) : CatalogUiState
}

@Composable
private fun SequoCatalogLoadingSkeleton() {
    val shimmer = rememberSequoShimmerBrush()
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SequoSkeletonPromo(shimmer)
        SequoSkeletonCategoryRow(shimmer)
        SequoSkeletonProductSection(shimmer)
        SequoSkeletonShopSection(shimmer)
    }
}

@Composable
private fun rememberSequoShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "catalogShimmer")
    val shimmerOffset by transition.animateFloat(
        initialValue = -420f,
        targetValue = 920f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1250, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "catalogShimmerOffset",
    )
    val base = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
    val glow = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
    val edge = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.24f)
    return Brush.linearGradient(
        colors = listOf(base, glow, edge),
        start = Offset(shimmerOffset, 0f),
        end = Offset(shimmerOffset + 360f, 260f),
    )
}

@Composable
private fun SequoSkeletonPromo(shimmer: Brush) {
    SequoCard(shape = RoundedCornerShape(30.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(168.dp)
                .padding(18.dp),
        ) {
            Column(
                modifier = Modifier.align(Alignment.CenterStart).fillMaxWidth(0.58f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ShimmerBlock(shimmer, widthFraction = 0.38f, height = 14.dp, shape = RoundedCornerShape(999.dp))
                ShimmerBlock(shimmer, widthFraction = 0.94f, height = 24.dp, shape = RoundedCornerShape(10.dp))
                ShimmerBlock(shimmer, widthFraction = 0.72f, height = 14.dp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ShimmerBlock(shimmer, modifier = Modifier.width(78.dp), height = 32.dp, shape = RoundedCornerShape(999.dp))
                    ShimmerBlock(shimmer, modifier = Modifier.width(58.dp), height = 32.dp, shape = RoundedCornerShape(999.dp))
                }
            }
            ShimmerBlock(
                shimmer = shimmer,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(118.dp),
                shape = RoundedCornerShape(32.dp),
            )
        }
    }
}

@Composable
private fun SequoSkeletonCategoryRow(shimmer: Brush) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ShimmerBlock(shimmer, widthFraction = 0.34f, height = 18.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(4) { index ->
                Surface(
                    modifier = Modifier.weight(1f).height(74.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f)),
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        ShimmerBlock(shimmer, modifier = Modifier.size(28.dp), shape = RoundedCornerShape(10.dp))
                        ShimmerBlock(shimmer, widthFraction = if (index % 2 == 0) 0.78f else 0.58f, height = 10.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SequoSkeletonProductSection(shimmer: Brush) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ShimmerSectionHeader(shimmer)
        repeat(2) {
            SequoCard(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = 0.76f)) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ShimmerBlock(shimmer, modifier = Modifier.size(74.dp), shape = RoundedCornerShape(18.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ShimmerBlock(shimmer, widthFraction = 0.88f, height = 14.dp)
                        ShimmerBlock(shimmer, widthFraction = 0.56f, height = 11.dp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ShimmerBlock(shimmer, modifier = Modifier.width(68.dp), height = 24.dp, shape = RoundedCornerShape(999.dp))
                            ShimmerBlock(shimmer, modifier = Modifier.width(48.dp), height = 24.dp, shape = RoundedCornerShape(999.dp))
                        }
                    }
                    ShimmerBlock(shimmer, modifier = Modifier.size(34.dp), shape = RoundedCornerShape(12.dp))
                }
            }
        }
    }
}

@Composable
private fun SequoSkeletonShopSection(shimmer: Brush) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ShimmerSectionHeader(shimmer)
        repeat(2) {
            SequoCard(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = 0.76f)) {
                Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(11.dp), verticalAlignment = Alignment.CenterVertically) {
                        ShimmerBlock(shimmer, modifier = Modifier.size(42.dp), shape = RoundedCornerShape(14.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            ShimmerBlock(shimmer, widthFraction = 0.74f, height = 14.dp)
                            ShimmerBlock(shimmer, widthFraction = 0.46f, height = 10.dp)
                        }
                        ShimmerBlock(shimmer, modifier = Modifier.width(42.dp), height = 22.dp, shape = RoundedCornerShape(999.dp))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ShimmerBlock(shimmer, modifier = Modifier.width(64.dp), height = 24.dp, shape = RoundedCornerShape(999.dp))
                        ShimmerBlock(shimmer, modifier = Modifier.width(74.dp), height = 24.dp, shape = RoundedCornerShape(999.dp))
                        ShimmerBlock(shimmer, modifier = Modifier.width(54.dp), height = 24.dp, shape = RoundedCornerShape(999.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ShimmerSectionHeader(shimmer: Brush) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ShimmerBlock(shimmer, widthFraction = 0.36f, height = 18.dp)
        ShimmerBlock(shimmer, modifier = Modifier.width(72.dp), height = 24.dp, shape = RoundedCornerShape(999.dp))
    }
}

@Composable
private fun ShimmerBlock(
    shimmer: Brush,
    modifier: Modifier = Modifier,
    widthFraction: Float? = null,
    height: Dp? = null,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp),
) {
    val sizedModifier = when {
        widthFraction != null && height != null -> modifier.fillMaxWidth(widthFraction).height(height)
        height != null -> modifier.height(height)
        else -> modifier
    }
    Box(
        modifier = sizedModifier
            .clip(shape)
            .background(shimmer),
    )
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

@Composable
private fun DrawerQuickLinkGrid(
    onDestinationSelected: (SequoSection) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            DrawerQuickLink(
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                label = appText(Res.string.drawer_orders),
                modifier = Modifier.weight(1f),
                onClick = { onDestinationSelected(SequoSection.Orders) },
            )
            DrawerQuickLink(
                icon = Icons.Filled.ShoppingBasket,
                label = appText(Res.string.drawer_cart),
                modifier = Modifier.weight(1f),
                onClick = { onDestinationSelected(SequoSection.Basket) },
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            DrawerQuickLink(
                icon = Icons.Filled.Notifications,
                label = appText(Res.string.drawer_alerts),
                modifier = Modifier.weight(1f),
                onClick = { onDestinationSelected(SequoSection.Notifications) },
            )
            DrawerQuickLink(
                icon = Icons.Filled.Person,
                label = appText(Res.string.drawer_profile),
                modifier = Modifier.weight(1f),
                onClick = { onDestinationSelected(SequoSection.Account) },
            )
        }
    }
}

@Composable
private fun DrawerQuickLink(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier.height(48.dp),
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.56f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun DrawerSettingsPanel(
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    themePreference: AppThemePreference,
    onThemePreferenceChange: (AppThemePreference) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.56f))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        DrawerSegmentedSetting(
            icon = Icons.Filled.Translate,
            title = appText(Res.string.drawer_language),
            options = listOf(
                AppLanguage.English to appText(Res.string.drawer_language_en),
                AppLanguage.French to appText(Res.string.drawer_language_fr),
            ),
            selected = language,
            onSelected = onLanguageChange,
        )
        DrawerSegmentedSetting(
            icon = Icons.Filled.DarkMode,
            title = appText(Res.string.drawer_theme),
            options = listOf(
                AppThemePreference.System to appText(Res.string.drawer_theme_system),
                AppThemePreference.Light to appText(Res.string.drawer_theme_light),
                AppThemePreference.Dark to appText(Res.string.drawer_theme_dark),
            ),
            selected = themePreference,
            onSelected = onThemePreferenceChange,
        )
    }
}

@Composable
private fun <T> DrawerSegmentedSetting(
    icon: ImageVector,
    title: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelected: (T) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            options.forEach { (value, label) ->
                val isSelected = value == selected
                Surface(
                    modifier = Modifier.weight(1f).height(34.dp),
                    onClick = { onSelected(value) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface.copy(alpha = 0.76f),
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f)),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 6.dp)) {
                        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}
