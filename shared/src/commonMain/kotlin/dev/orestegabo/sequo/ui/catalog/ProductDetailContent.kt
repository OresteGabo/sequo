package dev.orestegabo.sequo.ui.catalog

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.orestegabo.sequo.data.*
import dev.orestegabo.sequo.domain.*
import dev.orestegabo.sequo.feature.settings.appText
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
import kotlin.math.abs
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.painterResource
import sequo.shared.generated.resources.*

@Composable
internal fun ProductDetailContent(
    listing: SequoProductListing,
    allShops: List<SequoShop>,
    onAddProduct: () -> Unit,
    onProductSelected: (SequoProductListing) -> Unit,
    onNegotiateClick: () -> Unit,
) {
    val product = listing.product
    val shop = listing.shop
    val suggestions = remember(listing, allShops) { similarProductListings(listing, allShops) }
    var galleryExpanded by remember(listing) { mutableStateOf(false) }
    var selectedPhotoIndex by remember(listing) { mutableStateOf(0) }
    val galleryProducts = remember(product, suggestions) { productGalleryProducts(product, suggestions) }

    ProductDetailHero(
        product = product,
        galleryProducts = galleryProducts,
        selectedPhotoIndex = selectedPhotoIndex,
        galleryExpanded = galleryExpanded,
        onNextPhoto = { selectedPhotoIndex = (selectedPhotoIndex + 1) % galleryProducts.size },
        onOpenGallery = { galleryExpanded = true },
    )
    Column(
        modifier = Modifier
            .offset(y = (-28).dp)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (galleryExpanded) {
            ProductPhotoStack(
                galleryProducts = galleryProducts,
                selectedPhotoIndex = selectedPhotoIndex,
                onPhotoSelected = { selectedPhotoIndex = it },
                onClose = { galleryExpanded = false },
            )
        }
        ProductDetailInfoCard(
            listing = listing,
            onAddProduct = onAddProduct,
            onNegotiateClick = onNegotiateClick,
        )
        ProductSellerCard(shop = shop)
        ProductSuggestionsSection(
            current = listing,
            allShops = allShops,
            suggestions = suggestions,
            onAddProduct = onAddProduct,
            onProductSelected = onProductSelected,
        )
    }
}

@Composable
private fun ProductPhotoStack(
    galleryProducts: List<SequoProduct>,
    selectedPhotoIndex: Int,
    onPhotoSelected: (Int) -> Unit,
    onClose: () -> Unit,
) {
    val selectedProduct = galleryProducts[selectedPhotoIndex]
    val stackedProducts = galleryProducts
        .filterIndexed { index, _ -> index != selectedPhotoIndex }
        .take(3)
    var photoActionProduct by remember { mutableStateOf<SequoProduct?>(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(246.dp),
        contentAlignment = Alignment.Center,
    ) {
        stackedProducts.reversed().forEachIndexed { index, galleryProduct ->
            val visualIndex = 2 - index
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.74f)
                    .height(184.dp)
                    .offset(x = ((visualIndex - 1) * 20).dp, y = ((visualIndex + 1) * 8).dp)
                    .rotate((visualIndex - 1) * 5f),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.62f)),
                shadowElevation = 4.dp,
            ) {
                Image(
                    painter = painterResource(productImageResource(galleryProduct)),
                    contentDescription = galleryProduct.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                PhotoAuthenticityBadge(
                    product = galleryProduct,
                    compact = true,
                    onDark = true,
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                )
            }
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .height(214.dp)
                .pointerInput(selectedProduct.id) {
                    detectTapGestures(
                        onTap = { onPhotoSelected((selectedPhotoIndex + 1) % galleryProducts.size) },
                        onLongPress = { photoActionProduct = selectedProduct },
                    )
                },
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
            shadowElevation = 8.dp,
        ) {
            Box(Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(productImageResource(selectedProduct)),
                    contentDescription = selectedProduct.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                PhotoAuthenticityBadge(
                    product = selectedProduct,
                    onDark = true,
                    modifier = Modifier.align(Alignment.TopStart).padding(10.dp),
                )
                Surface(
                    onClick = onClose,
                    modifier = Modifier.align(Alignment.TopEnd).padding(10.dp).size(34.dp),
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.52f),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close photos",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DetailChip(Icons.Filled.PhotoLibrary, "${selectedPhotoIndex + 1}/${galleryProducts.size}")
                    DetailChip(Icons.Filled.TouchApp, "Tap next")
                }
            }
        }
        photoActionProduct?.let { actionProduct ->
            ProductPhotoActionDialog(
                product = actionProduct,
                onDismiss = { photoActionProduct = null },
            )
        }
        Row(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            galleryProducts.indices.forEach { index ->
                Box(
                    modifier = Modifier
                        .size(width = if (index == selectedPhotoIndex) 18.dp else 7.dp, height = 7.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (index == selectedPhotoIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                        .clickable { onPhotoSelected(index) },
                )
            }
        }
    }
}

@Composable
private fun ProductDetailHero(
    product: SequoProduct,
    galleryProducts: List<SequoProduct>,
    selectedPhotoIndex: Int,
    galleryExpanded: Boolean,
    onNextPhoto: () -> Unit,
    onOpenGallery: () -> Unit,
) {
    val selectedProduct = galleryProducts[selectedPhotoIndex]
    var heroSettled by remember(product.id) { mutableStateOf(false) }
    var photoActionProduct by remember { mutableStateOf<SequoProduct?>(null) }
    LaunchedEffect(product.id) {
        heroSettled = true
    }
    val heroHeight by animateDpAsState(
        targetValue = if (heroSettled) 348.dp else 182.dp,
        animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
        label = "productHeroHeight",
    )
    val heroHorizontalPadding by animateDpAsState(
        targetValue = if (heroSettled) 0.dp else 22.dp,
        animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
        label = "productHeroHorizontalPadding",
    )
    val heroCornerRadius by animateDpAsState(
        targetValue = if (heroSettled) 0.dp else 24.dp,
        animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
        label = "productHeroCornerRadius",
    )
    val heroChromeAlpha by animateFloatAsState(
        targetValue = if (heroSettled) 1f else 0f,
        animationSpec = tween(durationMillis = 260, delayMillis = 180),
        label = "productHeroChromeAlpha",
    )
    Box(modifier = Modifier.fillMaxWidth().height(348.dp)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(heroHeight)
                .align(Alignment.TopCenter)
                .padding(horizontal = heroHorizontalPadding)
                .pointerInput(selectedProduct.id) {
                    detectTapGestures(onLongPress = { photoActionProduct = selectedProduct })
                },
            shape = RoundedCornerShape(heroCornerRadius),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = if (heroSettled) 0.dp else 8.dp,
        ) {
            Box(Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(productImageResource(selectedProduct)),
                    contentDescription = selectedProduct.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                PhotoAuthenticityBadge(
                    product = selectedProduct,
                    onDark = true,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(top = 66.dp, end = 20.dp)
                        .graphicsLayer { alpha = heroChromeAlpha },
                )
                Box(
                    Modifier
                        .matchParentSize()
                        .graphicsLayer { alpha = heroChromeAlpha }
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.54f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.50f),
                                    MaterialTheme.colorScheme.background,
                                ),
                            ),
                        ),
                )
            }
        }
        photoActionProduct?.let { actionProduct ->
            ProductPhotoActionDialog(
                product = actionProduct,
                onDismiss = { photoActionProduct = null },
            )
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, end = 20.dp, bottom = 46.dp)
                .graphicsLayer { alpha = heroChromeAlpha },
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (product.isNegotiable) DetailChip(Icons.Filled.LocalOffer, "Offer")
            if (product.hasDiscount) DetailChip(Icons.Filled.Percent, "Discount")
        }
        if (!galleryExpanded) {
            MiniPhotoStack(
                galleryProducts = galleryProducts,
                selectedPhotoIndex = selectedPhotoIndex,
                onNextPhoto = onNextPhoto,
                onOpenGallery = onOpenGallery,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 34.dp)
                    .graphicsLayer { alpha = heroChromeAlpha },
            )
        }
    }
}

@Composable
private fun MiniPhotoStack(
    galleryProducts: List<SequoProduct>,
    selectedPhotoIndex: Int,
    onNextPhoto: () -> Unit,
    onOpenGallery: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedProduct = galleryProducts[selectedPhotoIndex]
    val stackedProducts = galleryProducts
        .filterIndexed { index, _ -> index != selectedPhotoIndex }
        .take(3)
    var photoActionProduct by remember { mutableStateOf<SequoProduct?>(null) }

    Box(
        modifier = modifier
            .size(width = 106.dp, height = 92.dp)
            .pointerInput(selectedProduct.id) {
                detectTapGestures(
                    onTap = { onNextPhoto() },
                    onLongPress = { photoActionProduct = selectedProduct },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        stackedProducts.reversed().forEachIndexed { index, galleryProduct ->
            val visualIndex = 2 - index
            Surface(
                modifier = Modifier
                    .size(width = 72.dp, height = 62.dp)
                    .offset(x = ((visualIndex - 1) * 7).dp, y = ((visualIndex + 1) * 3).dp)
                    .rotate((visualIndex - 1) * 5f),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.70f)),
                shadowElevation = 3.dp,
            ) {
                Image(
                    painter = painterResource(productImageResource(galleryProduct)),
                    contentDescription = galleryProduct.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                PhotoAuthenticityBadge(
                    product = galleryProduct,
                    compact = true,
                    onDark = true,
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp),
                )
            }
        }
        Surface(
            modifier = Modifier.size(width = 82.dp, height = 72.dp),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.78f)),
            shadowElevation = 6.dp,
        ) {
            Box(Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(productImageResource(selectedProduct)),
                    contentDescription = selectedProduct.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                PhotoAuthenticityBadge(
                    product = selectedProduct,
                    compact = true,
                    onDark = true,
                    modifier = Modifier.align(Alignment.TopStart).padding(5.dp),
                )
                Surface(
                    onClick = onOpenGallery,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(5.dp),
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.52f),
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoLibrary,
                        contentDescription = "Open photos",
                        tint = Color.White,
                        modifier = Modifier.padding(5.dp).size(14.dp),
                    )
                }
            }
        }
        photoActionProduct?.let { actionProduct ->
            ProductPhotoActionDialog(
                product = actionProduct,
                onDismiss = { photoActionProduct = null },
            )
        }
    }
}

@Composable
private fun ProductDetailInfoCard(
    listing: SequoProductListing,
    onAddProduct: () -> Unit,
    onNegotiateClick: () -> Unit,
) {
    val product = listing.product
    SequoCard(shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(product.detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(product.optionHint, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f))
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.38f))
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(formatCfa(product.priceCfa), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                    product.originalPriceCfa?.takeIf { product.hasDiscount }?.let { original ->
                        Text(
                            "Was ${formatCfa(original)} / save ${formatCfa(original - product.priceCfa)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                ProductDiscountBadge(product = product)
            }
            ProductConcernPanel(product = product)
            if (product.isNegotiable) {
                Surface(
                    onClick = onNegotiateClick,
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.LocalOffer, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(appText(Res.string.home_make_offer), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(product.bargainNote ?: "Send a price proposal to the seller.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            SequoPrimaryButton("Add to cart", onAddProduct, Modifier.fillMaxWidth())
        }
    }
}
@Composable
private fun ProductConcernPanel(product: SequoProduct) {
    val isFood = product.isFoodOrGrocery()
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.56f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.36f)),
    ) {
        Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isFood) Icons.Filled.Restaurant else Icons.Filled.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    if (isFood) "Food notes" else "Product notes",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (isFood) {
                ProductNoteRow(Icons.AutoMirrored.Filled.MenuBook, "Recipe idea", product.recipeSuggestion())
                ProductNoteRow(Icons.Filled.AcUnit, "Storage", product.storageSuggestion())
                ProductNoteRow(Icons.Filled.Inventory2, "Packaging", "Sealed bag or tray; keep separate from household items.")
                ProductNoteRow(Icons.Filled.Schedule, "Use soon", "Best checked at pickup and used within the freshness window.")
            } else {
                ProductNoteRow(Icons.Filled.CheckCircle, "Check", "Confirm size, color, and condition before checkout.")
                ProductNoteRow(Icons.Filled.Inventory2, "Packaging", "Seller packaging shown before delivery when available.")
                ProductNoteRow(Icons.Filled.LocalOffer, "Price", if (product.isNegotiable) "Offers are available for this item." else "Listed as fixed price.")
            }
        }
    }
}

@Composable
private fun ProductNoteRow(icon: ImageVector, title: String, detail: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(9.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(17.dp).padding(top = 1.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ProductSellerCard(shop: SequoShop) {
    SequoCard(shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                SequoShoppingMark(Modifier.size(46.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(shop.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${shop.area} / ${shop.kind}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                RatingMark(shop.rating)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MetaPill(formatDistance(shop.distanceKm), SequoPrimary)
                MetaPill(formatCfa(baseDelivery(shop.distanceKm)), SequoSecondary)
                MetaPill(shop.eta, SequoAccent)
            }
            RuleRow(shop.openStatus, shop.consolidation)
        }
    }
}

private fun SequoProduct.isFoodOrGrocery(): Boolean {
    val text = "$name $detail $label $subcategory".lowercase()
    return listOf(
        "food",
        "grocery",
        "produce",
        "meat",
        "seafood",
        "milk",
        "lait",
        "boeuf",
        "tilapia",
        "saumon",
        "poivron",
        "tomate",
        "farine",
        "cajou",
        "coco",
        "soja",
    ).any { it in text }
}

private fun SequoProduct.recipeSuggestion(): String {
    val text = "$name $detail $label".lowercase()
    return when {
        "poivron" in text -> "Slice into sauces, omelets, fried rice, or grilled meat plates."
        "boeuf" in text || "beef" in text || "steak" in text -> "Works for skewers, quick stew, stir-fry, or pepper sauce."
        "tilapia" in text || "poisson" in text || "fish" in text -> "Good grilled, fried, or cooked in tomato-onion sauce."
        "saumon" in text || "salmon" in text -> "Pan-sear, oven-bake, or pair with rice and vegetables."
        "lait" in text || "milk" in text -> "Use for breakfast, smoothies, baking, or tea/coffee."
        "farine" in text || "flour" in text -> "Useful for bread, pancakes, fritters, and simple batters."
        "cajou" in text || "cashew" in text -> "Snack, blend into sauces, or add crunch to rice and salads."
        "coco" in text || "coconut" in text -> "Use fresh, grate into desserts, or add to rice and sauces."
        "soja" in text || "soy" in text -> "Soak before cooking; good for stews, milk, or protein-rich meals."
        else -> "Use in everyday meals; check freshness and quantity before checkout."
    }
}

private fun SequoProduct.storageSuggestion(): String {
    val text = "$name $detail $optionHint".lowercase()
    return when {
        "chilled" in text || "fresh milk" in text || "lait" in text -> "Keep cold and deliver quickly."
        "frozen" in text -> "Keep frozen until preparation."
        "meat" in text || "boeuf" in text || "fish" in text || "poisson" in text || "saumon" in text -> "Keep chilled; request sealed cold packaging."
        "dry" in text || "farine" in text || "soja" in text || "beans" in text -> "Store dry, sealed, and away from humidity."
        else -> "Keep cool, clean, and separate from cleaning products."
    }
}

@Composable
private fun ProductSuggestionsSection(
    current: SequoProductListing,
    allShops: List<SequoShop>,
    suggestions: List<SequoProductListing>,
    onAddProduct: () -> Unit,
    onProductSelected: (SequoProductListing) -> Unit,
) {
    var selectedTab by remember(current) { mutableStateOf(ProductSuggestionTab.SameShop) }
    val sameShopOptions = remember(current, allShops) { sameShopProductListings(current, allShops) }
    val otherShopOptions = remember(current, suggestions) {
        suggestions.filterNot { it.shop.name == current.shop.name }
    }
    val visibleSuggestions = when (selectedTab) {
        ProductSuggestionTab.SameShop -> sameShopOptions
        ProductSuggestionTab.OtherShops -> otherShopOptions
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MarketplaceSectionHeader(
            title = "More buying options",
            action = when (selectedTab) {
                ProductSuggestionTab.SameShop -> "One delivery"
                ProductSuggestionTab.OtherShops -> "Compare"
            },
            onAction = {},
        )
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
        ) {
            PrimaryTabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                ProductSuggestionTab.entries.forEach { tab ->
                    val count = when (tab) {
                        ProductSuggestionTab.SameShop -> sameShopOptions.size
                        ProductSuggestionTab.OtherShops -> otherShopOptions.size
                    }
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                Text(tab.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    "$count items",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selectedTab == tab) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                )
                            }
                        },
                    )
                }
            }
        }
        Text(
            suggestionExplainer(selectedTab, current),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (visibleSuggestions.isEmpty()) {
            Text(
                when (selectedTab) {
                    ProductSuggestionTab.SameShop -> "No other products from this seller yet."
                    ProductSuggestionTab.OtherShops -> "No close alternatives from other shops yet."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            visibleSuggestions.forEach { suggestion ->
                CompactProductCard(
                    shop = suggestion.shop,
                    product = suggestion.product,
                    comparisonPriceCfa = current.product.priceCfa,
                    relationLabel = productRelationLabel(current.product),
                    onAddProduct = onAddProduct,
                    onProductClick = { onProductSelected(suggestion) },
                )
            }
        }
    }
}

private enum class ProductSuggestionTab {
    SameShop,
    OtherShops;

    val label: String
        get() = when (this) {
            SameShop -> "Same shop"
            OtherShops -> "Other shops"
        }
}

@Composable
private fun DetailChip(icon: ImageVector, label: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color.White.copy(alpha = 0.90f),
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.08f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        }
    }
}

private fun similarProductListings(current: SequoProductListing, allShops: List<SequoShop>): List<SequoProductListing> {
    val currentRelationKey = productRelationKey(current.product)
    val currentSubcategory = normalizedProductSubcategory(current.product)
    return allShops
        .flatMap { shop -> shop.products.map { product -> SequoProductListing(shop, product) } }
        .filterNot { it.shop.name == current.shop.name && it.product.name == current.product.name }
        .filter { listing ->
            val product = listing.product
            productRelationKey(product) == currentRelationKey ||
                normalizedProductSubcategory(product) == currentSubcategory ||
                product.label == current.product.label ||
                listing.shop.kind == current.shop.kind
        }
        .sortedWith(
            compareByDescending<SequoProductListing> { listing ->
                when {
                    productRelationKey(listing.product) == currentRelationKey -> 4
                    normalizedProductSubcategory(listing.product) == currentSubcategory -> 3
                    listing.product.label == current.product.label -> 2
                    listing.shop.kind == current.shop.kind -> 1
                    else -> 0
                }
            }.thenBy { listing ->
                abs(listing.product.priceCfa - current.product.priceCfa)
            }.thenBy { listing ->
                listing.shop.distanceKm
            },
        )
        .take(6)
}

private fun sameShopProductListings(current: SequoProductListing, allShops: List<SequoShop>): List<SequoProductListing> {
    val currentRelationKey = productRelationKey(current.product)
    val currentSubcategory = normalizedProductSubcategory(current.product)
    val currentShop = allShops.firstOrNull { it.name == current.shop.name } ?: current.shop
    return currentShop.products
        .filterNot { it.name == current.product.name }
        .sortedWith(
            compareByDescending<SequoProduct> { product ->
                when {
                    productRelationKey(product) == currentRelationKey -> 4
                    normalizedProductSubcategory(product) == currentSubcategory -> 3
                    product.label == current.product.label -> 2
                    else -> 1
                }
            }.thenBy { product ->
                abs(product.priceCfa - current.product.priceCfa)
            },
        )
        .map { product -> SequoProductListing(current.shop, product) }
        .take(6)
}

private fun suggestionExplainer(tab: ProductSuggestionTab, current: SequoProductListing): String {
    val relation = productRelationLabel(current.product).lowercase()
    return when (tab) {
        ProductSuggestionTab.SameShop -> "More from ${current.shop.name}; closest $relation alternatives stay first."
        ProductSuggestionTab.OtherShops -> "Similar $relation options from other sellers, sorted by closest match and price."
    }
}

private fun productRelationLabel(product: SequoProduct): String =
    when (productRelationKey(product)) {
        "smartphones" -> "smartphone"
        "laptops" -> "laptop"
        "jollof-rice" -> "jollof plate"
        "attieke-fish" -> "attieke fish plate"
        "grilled-food" -> "grilled food"
        "rice-bag" -> "rice bag"
        "cooking-oil" -> "cooking oil"
        "power-bank" -> "power bank"
        "earbuds" -> "earbuds"
        "t-shirt" -> "T-shirt"
        "sanitizer" -> "sanitizer"
        "pastry" -> "pastry"
        else -> productSubcategory(product)
    }

private fun productRelationKey(product: SequoProduct): String {
    val text = "${product.name} ${product.detail} ${product.label} ${product.subcategory} ${product.optionHint}".lowercase()
    return when {
        "iphone" in text || "smartphone" in text || "poco" in text -> "smartphones"
        ("laptop" in text || "ordinateur" in text) && "sleeve" !in text -> "laptops"
        "jollof" in text || "riz gras" in text -> "jollof-rice"
        "attieke" in text && ("fish" in text || "tilapia" in text || "poisson" in text) -> "attieke-fish"
        "grill" in text || "brochette" in text || "shawarma" in text || "kebab" in text -> "grilled-food"
        "rice bag" in text || "local rice" in text -> "rice-bag"
        "vegetable oil" in text || "palm oil" in text || "huile" in text -> "cooking-oil"
        "power bank" in text -> "power-bank"
        "earbuds" in text || "headset" in text -> "earbuds"
        "tee" in text || "t-shirt" in text -> "t-shirt"
        "sanitizer" in text -> "sanitizer"
        "croissant" in text || "pastry" in text || "pain au lait" in text -> "pastry"
        else -> normalizedProductSubcategory(product)
    }
}

private fun normalizedProductSubcategory(product: SequoProduct): String =
    productSubcategory(product).lowercase().trim()

private fun productGalleryProducts(
    product: SequoProduct,
    suggestions: List<SequoProductListing>,
): List<SequoProduct> =
    buildList {
        add(product)
        suggestions
            .map { it.product }
            .filterNot { it.name == product.name }
            .forEach { add(it) }
        while (size < 4) {
            add(product)
        }
    }.take(4)
