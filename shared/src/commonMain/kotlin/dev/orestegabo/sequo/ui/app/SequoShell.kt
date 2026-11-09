package dev.orestegabo.sequo.ui.app

import dev.orestegabo.sequo.core.platform.PlatformBackHandler

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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequo.core.auth.CurrentUser
import dev.orestegabo.sequo.core.auth.SavedDeliveryAddress
import dev.orestegabo.sequo.core.catalog.CatalogApiClient
import dev.orestegabo.sequo.core.catalog.CatalogSnapshot
import dev.orestegabo.sequo.feature.legal.LegalInitialTab
import dev.orestegabo.sequo.feature.settings.AppLanguage
import dev.orestegabo.sequo.feature.settings.AppThemePreference
import dev.orestegabo.sequo.feature.settings.appCatalogText
import dev.orestegabo.sequo.feature.settings.appText
import dev.orestegabo.sequo.data.*
import dev.orestegabo.sequo.design.SequoDesign
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import dev.orestegabo.sequo.core.bargaining.BargainingRepository
import dev.orestegabo.sequo.core.bargaining.NetworkBargainingRepository
import dev.orestegabo.sequo.core.catalog.CartApiClient
import dev.orestegabo.sequo.core.catalog.CartSnapshot
import dev.orestegabo.sequo.core.catalog.CustomerBenefits
import org.jetbrains.compose.resources.painterResource
import sequo.shared.generated.resources.*

@Composable
internal fun SequoShell(
    currentUser: CurrentUser?,
    cartAccessToken: suspend () -> String?,
    loadSavedAddresses: suspend () -> List<SavedDeliveryAddress>,
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
    val cartClient = remember { CartApiClient() }
    DisposableEffect(cartClient) { onDispose(cartClient::close) }
    val bargainingRepository = remember(currentUser?.id, isGuest) {
        if (isGuest || currentUser == null) null else NetworkBargainingRepository(cartAccessToken)
    }
    DisposableEffect(bargainingRepository) { onDispose { bargainingRepository?.close() } }
    var cart by remember(currentUser?.id, isGuest) { mutableStateOf<CartSnapshot?>(null) }
    var cartBusy by remember(currentUser?.id, isGuest) { mutableStateOf(false) }
    var cartFailed by remember(currentUser?.id, isGuest) { mutableStateOf(false) }
    var searchVisible by remember { mutableStateOf(false) }
    var selectedMarketTypeKey by remember { mutableStateOf("") }
    var browseMode by remember { mutableStateOf(CatalogBrowseMode.All) }
    var selectedProductListing by remember { mutableStateOf<SequoProductListing?>(null) }
    var catalogState by remember { mutableStateOf<CatalogUiState>(CatalogUiState.Loading) }
    var notificationUnreadCount by remember { mutableStateOf(defaultNotificationUnreadCount()) }
    val catalogClient = remember { CatalogApiClient() }
    val categoryUsage = remember { mutableStateMapOf<String, Int>() }
    val pinnedCategoryKeys = remember { mutableStateListOf("food") }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val basketCount = cart?.totalItems ?: 0
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val tapFeedback = dev.orestegabo.sequo.feature.settings.LocalShoppingPreferences.current.value.tapFeedback
    val catalogLoadError = appText(Res.string.home_catalog_load_error)

    suspend fun changeCart(change: suspend (String) -> CartSnapshot): CartSnapshot? {
        if (cartBusy) return null
        cartBusy = true
        cartFailed = false
        try {
            return change(checkNotNull(cartAccessToken())).also { cart = it }
        } finally {
            cartBusy = false
        }
    }

    fun updateCart(change: suspend (String) -> CartSnapshot) {
        scope.launch {
            try {
                changeCart(change)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                cartFailed = true
                selectedProductListing = null
                currentDestination = SequoSection.Basket
            }
        }
    }

    suspend fun saveProduct(token: String, productId: String, selections: List<ProductOptionSelection>, bargainingThreadId: String? = null): CartSnapshot {
        val saved = cartClient.cart(token)
        val quantity = saved.findLine(productId, selections, bargainingThreadId)?.quantity ?: 0
        return cartClient.put(token, productId, quantity + 1, selections, bargainingThreadId).also {
            if (tapFeedback) runCatching { haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove) }
        }
    }

    suspend fun addConfiguredProduct(productId: String, selections: List<ProductOptionSelection>): Int? {
        if (isGuest) {
            onSignInRequested()
            return null
        }
        val saved = checkNotNull(changeCart { saveProduct(it, productId, selections) })
        val line = checkNotNull(saved.findLine(productId, selections))
        return checkNotNull(line.lineTotalCfa) / line.quantity
    }

    suspend fun addNegotiatedProduct(productId: String, selections: List<ProductOptionSelection>, threadId: String): Int? {
        if (isGuest) { onSignInRequested(); return null }
        val saved = checkNotNull(changeCart { saveProduct(it, productId, selections, threadId) })
        val line = checkNotNull(saved.findLine(productId, selections, threadId))
        return checkNotNull(line.lineTotalCfa) / line.quantity
    }

    LaunchedEffect(currentUser?.id, isGuest, currentDestination) {
        if (!isGuest && currentUser != null && (cart == null || currentDestination == SequoSection.Basket)) {
            updateCart(cartClient::cart)
        }
    }

    fun reloadCatalog() {
        scope.launch {
            catalogState = CatalogUiState.Loading
            catalogState = runCatching { catalogClient.home() }
                .fold(
                    onSuccess = { snapshot ->
                        if (selectedMarketTypeKey.isNotBlank() && snapshot.categories.none { it.key == selectedMarketTypeKey }) {
                            selectedMarketTypeKey = ""
                        }
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

    val backAction = customerBackAction(
        drawerOpen = drawerState.isOpen,
        searchOpen = searchVisible,
        productOpen = selectedProductListing != null,
        hasNotificationOrigin = currentDestination == SequoSection.Notifications && notificationBackDestination != null,
        isHome = currentDestination == SequoSection.Home,
    )
    fun handleBack() {
        when (backAction) {
            CustomerBackAction.CloseDrawer -> { scope.launch { drawerState.close() } }
            CustomerBackAction.CloseSearch -> { searchVisible = false }
            CustomerBackAction.CloseProduct -> { selectedProductListing = null }
            CustomerBackAction.NotificationOrigin -> {
                currentDestination = notificationBackDestination ?: SequoSection.Home
                notificationBackDestination = null
                selectedProductListing = null
                searchVisible = false
            }
            CustomerBackAction.Home -> {
                currentDestination = SequoSection.Home
                notificationBackDestination = null
                selectedProductListing = null
                searchVisible = false
            }
            CustomerBackAction.None -> Unit
        }
    }
    // Child Material dialogs/drawers retain their own native Back handling.
    PlatformBackHandler(enabled = backAction != CustomerBackAction.None, onBack = ::handleBack)

    fun openCatalog(categoryKey: String = "", mode: CatalogBrowseMode = CatalogBrowseMode.All) {
        selectedMarketTypeKey = categoryKey
        browseMode = mode
        currentDestination = SequoSection.Markets
        notificationBackDestination = null
        searchVisible = false
        selectedProductListing = null
        scope.launch { drawerState.close() }
    }

    fun navigateTo(destination: SequoSection) {
        if (destination == SequoSection.Markets) {
            openCatalog()
        } else {
            currentDestination = destination
            notificationBackDestination = null
            searchVisible = false
            selectedProductListing = null
        }
    }
    val uriHandler = LocalUriHandler.current

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SequoNavigationDrawer(
                catalogState = catalogState,
                selectedMarketTypeKey = selectedMarketTypeKey.takeIf { currentDestination == SequoSection.Markets }.orEmpty(),
                selectedBrowseMode = browseMode.takeIf { currentDestination == SequoSection.Markets },
                onMarketTypeSelected = { typeKey ->
                    categoryUsage[typeKey] = (categoryUsage[typeKey] ?: 0) + 1
                    openCatalog(categoryKey = typeKey)
                },
                onShortcutSelected = { openCatalog(mode = it) },
                onReloadCatalog = ::reloadCatalog,
                onOpenHelp = { uriHandler.openUri("https://www.sequoservice.com/aide") },
            )
        },
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                SequoTopAppBar(
                    currentDestination = currentDestination,
                    sectionTitle = if (currentDestination == SequoSection.Markets && browseMode != CatalogBrowseMode.All) appText(browseMode.titleResource()) else null,
                    sectionSubtitle = if (currentDestination == SequoSection.Markets && browseMode != CatalogBrowseMode.All) appText(browseMode.detailResource()) else null,
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
                    onBackClick = ::handleBack,
                    modifier = Modifier.statusBarsPadding(),
                )
            },
            bottomBar = {
                SequoBottomBar(
                    currentDestination = currentDestination,
                    onDestinationSelected = ::navigateTo,
                    pendingBasketCount = basketCount,
                    notificationUnreadCount = notificationUnreadCount,
                    modifier = Modifier.navigationBarsPadding(),
                )
            },
        ) { contentPadding ->
            Box(Modifier.fillMaxSize().padding(contentPadding).consumeWindowInsets(contentPadding)) {
                SequoAmbientBackground(modifier = Modifier.fillMaxSize())
                SequoContentStage(
                    currentDestination = currentDestination,
                    onDestinationSelected = ::navigateTo,
                    selectedProductListing = selectedProductListing,
                    onProductSelected = { listing ->
                        selectedProductListing = listing
                        searchVisible = false
                    },
                    onCloseProduct = { selectedProductListing = null },
                    cartContent = {
                        BasketContent(
                            onBrowse = { openCatalog() },
                            cart = cart,
                            busy = cartBusy,
                            failed = cartFailed,
                            onRefresh = { updateCart(cartClient::cart) },
                            onQuantity = { line, quantity ->
                                updateCart { token ->
                                    if (quantity == 0) cartClient.remove(token, line.id)
                                    else cartClient.put(token, line.product.id, quantity, line.selections, line.bargainingThreadId)
                                }
                            },
                            loadAddresses = loadSavedAddresses,
                            loadQuote = { addressId, relayPointId, mode -> cartClient.quote(checkNotNull(cartAccessToken()), addressId, relayPointId, mode) },
                            loadRelayPoints = { cartClient.relayPoints(checkNotNull(cartAccessToken())) },
                            loadPaymentProviders = cartClient::paymentProviders,
                            submitCheckout = { quote, provider ->
                                cartClient.checkout(checkNotNull(cartAccessToken()), quote, provider).also {
                                    cart = CartSnapshot(emptyList(), 0, 0)
                                }
                            },
                            onOpenOrders = { currentDestination = SequoSection.Orders },
                        )
                    },
                    ordersContent = {
                        OrdersContent(
                            onBrowse = { openCatalog() },
                            loadCheckouts = { cartClient.checkouts(checkNotNull(cartAccessToken())) },
                            loadOrders = { cartClient.orders(checkNotNull(cartAccessToken())) },
                            sessionKey = currentUser?.id,
                            confirmTestPayment = {id->cartClient.confirmTestPayment(checkNotNull(cartAccessToken()),id)},
                            loadTestDispatch = {id->cartClient.testDispatch(checkNotNull(cartAccessToken()),id)},
                            assignTestRider = {id,rider->cartClient.assignTestRider(checkNotNull(cartAccessToken()),id,rider)},
                            loadDemoJourney = { id -> cartClient.demoJourney(checkNotNull(cartAccessToken()), id) },
                            advanceDemoJourney = { id, action, rider -> cartClient.advanceDemo(checkNotNull(cartAccessToken()), id, action, rider) },
                            loadRelayPickups = { orderId -> cartClient.relayPickups(checkNotNull(cartAccessToken()), orderId) },
                            renewRelayPickup = { orderId, parcelId -> cartClient.renewRelayPickup(checkNotNull(cartAccessToken()), orderId, parcelId) },
                            loadReturnEligibility = { orderId -> cartClient.returnEligibility(checkNotNull(cartAccessToken()), orderId) },
                            loadReturns = { orderId -> cartClient.returns(checkNotNull(cartAccessToken()), orderId) },
                            submitReturn = { orderId, returnId, reason -> cartClient.requestReturn(checkNotNull(cartAccessToken()), orderId, returnId, reason) },
                        )
                    },
                    notificationsContent = {
                        if (isGuest || currentUser == null) NotificationsContent(
                            onSignInRequested = onSignInRequested,
                            onOpenOrders = { currentDestination = SequoSection.Orders },
                            onOpenPrivacy = { onOpenLegal(LegalInitialTab.Privacy) },
                            onOpenTerms = { onOpenLegal(LegalInitialTab.Terms) },
                            onUnreadCountChanged = { notificationUnreadCount = it },
                        ) else LiveCustomerNotificationsContent(
                            accountId = currentUser.id,
                            accessToken = cartAccessToken,
                            onUnreadCountChanged = { notificationUnreadCount = it },
                            onSignInRequested = onSignInRequested,
                            onOpenOrders = { currentDestination = SequoSection.Orders },
                        )
                    },
                    searchVisible = searchVisible,
                    onCloseSearch = { searchVisible = false },
                    selectedMarketTypeKey = selectedMarketTypeKey,
                    browseMode = browseMode,
                    onBrowseCategorySelected = { selectedMarketTypeKey = it },
                    onResetBrowse = { openCatalog() },
                    language = language,
                    onLanguageChange = onLanguageChange,
                    themePreference = themePreference,
                    onThemePreferenceChange = onThemePreferenceChange,
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
                    onAddProduct = { productId ->
                        if (isGuest) onSignInRequested()
                        else updateCart { saveProduct(it, productId, emptyList()) }
                    },
                    onAddCustomized = ::addConfiguredProduct,
                    onAddNegotiated = ::addNegotiatedProduct,
                    bargainingRepository = bargainingRepository,
                    catalogState = catalogState,
                    onReloadCatalog = ::reloadCatalog,
                    currentUser = currentUser,
                    loadSavedAddresses = loadSavedAddresses,
                    loadBenefits = { cartClient.benefits(checkNotNull(cartAccessToken())) },
                    isGuest = isGuest,
                    onNotificationUnreadCountChanged = { notificationUnreadCount = it },
                    onOpenLegal = onOpenLegal,
                    onSignInRequested = onSignInRequested,
                    onLogout = onLogout,
                    modifier = Modifier.fillMaxSize(),
                )
            }
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
    cartContent: @Composable () -> Unit,
    ordersContent: @Composable () -> Unit,
    notificationsContent: @Composable () -> Unit,
    searchVisible: Boolean,
    onCloseSearch: () -> Unit,
    selectedMarketTypeKey: String,
    browseMode: CatalogBrowseMode,
    onBrowseCategorySelected: (String) -> Unit,
    onResetBrowse: () -> Unit,
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    themePreference: AppThemePreference,
    onThemePreferenceChange: (AppThemePreference) -> Unit,
    categoryUsage: Map<String, Int>,
    pinnedCategoryKeys: List<String>,
    onCategoryUsed: (String) -> Unit,
    onToggleCategoryPinned: (String) -> Unit,
    onAddProduct: (String) -> Unit,
    onAddCustomized: suspend (String, List<ProductOptionSelection>) -> Int?,
    onAddNegotiated: suspend (String, List<ProductOptionSelection>, String) -> Int?,
    bargainingRepository: BargainingRepository?,
    catalogState: CatalogUiState,
    onReloadCatalog: () -> Unit,
    currentUser: CurrentUser?,
    loadSavedAddresses: suspend () -> List<SavedDeliveryAddress>,
    loadBenefits: suspend () -> CustomerBenefits,
    isGuest: Boolean,
    onNotificationUnreadCountChanged: (Int) -> Unit,
    onOpenLegal: (LegalInitialTab) -> Unit,
    onSignInRequested: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchQuery by remember { mutableStateOf("") }
    val catalog = (catalogState as? CatalogUiState.Ready)?.snapshot
    val needsCatalog = selectedProductListing != null ||
        currentDestination == SequoSection.Home || currentDestination == SequoSection.Markets
    if (searchVisible) {
        SequoScreenColumn(modifier = modifier) {
            SequoSearchCard(query = searchQuery, onQueryChange = { searchQuery = it }, onClose = onCloseSearch)
            CatalogSearchContent(
                query = searchQuery,
                catalog = catalog,
                loading = catalogState is CatalogUiState.Loading,
                failed = catalogState is CatalogUiState.Failed,
                onReload = onReloadCatalog,
                onAddProduct = onAddProduct,
                onProductSelected = onProductSelected,
                onClearQuery = { searchQuery = "" },
                onBrowse = { onCloseSearch(); onDestinationSelected(SequoSection.Markets) },
            )
        }
    } else if (needsCatalog && catalogState is CatalogUiState.Loading) {
        SequoScreenColumn(modifier = modifier) {
            SequoCatalogLoadingSkeleton()
        }
    } else if (needsCatalog && catalogState is CatalogUiState.Failed) {
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
                onAddCustomized = onAddCustomized,
                allShops = catalog.shops,
                onAddProduct = onAddProduct,
                onProductSelected = onProductSelected,
                bargainingRepository = bargainingRepository,
                onSignInRequested = onSignInRequested,
                onAddNegotiated = onAddNegotiated,
            )
        }
    } else {
        SequoScreenColumn(modifier = modifier) {
            when (currentDestination) {
                SequoSection.Home -> if (catalog?.products?.isEmpty() == true) {
                    SequoEmptyState(
                        visual = EmptyStateVisual.Catalog,
                        title = appText(Res.string.empty_catalog_title), detail = appText(Res.string.empty_catalog_detail),
                        actionLabel = appText(Res.string.cart_refresh), onAction = onReloadCatalog,
                    )
                } else HomeContent(
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
                    browseMode = browseMode,
                    onCategorySelected = onBrowseCategorySelected,
                    onResetFilters = onResetBrowse,
                    onRefreshCatalog = onReloadCatalog,
                    onAddProduct = onAddProduct,
                    onProductSelected = onProductSelected,
                )
                SequoSection.Basket -> if (isGuest) {
                    GuestSignInPanel(
                        visual = EmptyStateVisual.Cart,
                        title = appText(Res.string.guest_basket_title),
                        detail = appText(Res.string.guest_basket_detail),
                        onSignIn = onSignInRequested,
                        onBrowse = { onDestinationSelected(SequoSection.Markets) },
                    )
                } else {
                    cartContent()
                }
                SequoSection.Orders -> if (isGuest) {
                    GuestSignInPanel(
                        visual = EmptyStateVisual.Orders,
                        title = appText(Res.string.guest_orders_title),
                        detail = appText(Res.string.guest_orders_detail),
                        onSignIn = onSignInRequested,
                        onBrowse = { onDestinationSelected(SequoSection.Markets) },
                    )
                } else {
                    ordersContent()
                }
                SequoSection.Notifications -> notificationsContent()
                SequoSection.Settings -> AccountContent(
                    currentUser = currentUser,
                    language = language,
                    onLanguageChange = onLanguageChange,
                    themePreference = themePreference,
                    onThemePreferenceChange = onThemePreferenceChange,
                    onOpenLegal = onOpenLegal,
                    loadSavedAddresses = loadSavedAddresses,
                    loadBenefits = loadBenefits,
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
    visual: EmptyStateVisual,
    title: String,
    detail: String,
    onSignIn: () -> Unit,
    onBrowse: () -> Unit,
) {
    SequoEmptyState(
        visual = visual, title = title, detail = detail, signInRequired = true,
        actionLabel = appText(Res.string.account_sign_in_create), onAction = onSignIn,
        secondaryLabel = appText(Res.string.empty_explore), onSecondaryAction = onBrowse,
    )
}

@Composable
internal fun SequoScreenColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = SequoDesign.ScreenGutter, vertical = SequoDesign.SectionGap),
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
            .padding(bottom = SequoDesign.SectionGap),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        content = content,
    )
}

@Composable
internal fun SequoNavigationDrawer(
    catalogState: CatalogUiState,
    selectedMarketTypeKey: String,
    selectedBrowseMode: CatalogBrowseMode?,
    onMarketTypeSelected: (String) -> Unit,
    onShortcutSelected: (CatalogBrowseMode) -> Unit,
    onReloadCatalog: () -> Unit,
    onOpenHelp: () -> Unit,
) {
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.width(316.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SequoShoppingMark(Modifier.size(40.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(appText(Res.string.drawer_explore), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(appText(Res.string.drawer_intro), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            HorizontalDivider()
            DrawerSectionLabel(appText(Res.string.drawer_shortcuts))
            listOf(CatalogBrowseMode.Nearby, CatalogBrowseMode.Offers, CatalogBrowseMode.Negotiable).forEach { mode ->
                NavigationDrawerItem(
                    label = {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(appText(mode.titleResource()), style = MaterialTheme.typography.titleSmall)
                            Text(appText(mode.detailResource()), style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    },
                    selected = selectedBrowseMode == mode,
                    onClick = { onShortcutSelected(mode) },
                    icon = { Icon(mode.icon(), contentDescription = null) },
                    shape = RoundedCornerShape(16.dp),
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            DrawerSectionLabel(appText(Res.string.drawer_shop_categories))
            when (catalogState) {
                CatalogUiState.Loading -> Text(appText(Res.string.drawer_catalog_loading), modifier = Modifier.padding(12.dp))
                is CatalogUiState.Failed -> {
                    Text(appText(Res.string.home_catalog_load_error), modifier = Modifier.padding(horizontal = 12.dp), style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = onReloadCatalog) { Text(appText(Res.string.cart_refresh)) }
                }
                is CatalogUiState.Ready -> {
                    if (catalogState.snapshot.categories.isEmpty()) {
                        Text(appText(Res.string.browse_catalog_empty), modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
                    }
                    catalogState.snapshot.categories.forEach { type ->
                        NavigationDrawerItem(
                            label = { Text(appCatalogText(type.title), maxLines = 2, overflow = TextOverflow.Ellipsis) },
                            selected = selectedBrowseMode == CatalogBrowseMode.All && selectedMarketTypeKey == type.key,
                            onClick = { onMarketTypeSelected(type.key) },
                            icon = { Icon(type.icon, contentDescription = null, tint = type.accent) },
                            shape = RoundedCornerShape(14.dp),
                        )
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            Surface(onClick = onOpenHelp, shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.SupportAgent, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(appText(Res.string.drawer_help), style = MaterialTheme.typography.titleSmall)
                        Text(appText(Res.string.drawer_help_detail), style = MaterialTheme.typography.bodySmall)
                    }
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun DrawerSectionLabel(text: String) {
    Text(text, modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 4.dp),
        style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
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
