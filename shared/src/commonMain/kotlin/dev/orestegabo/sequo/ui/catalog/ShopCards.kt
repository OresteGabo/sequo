package dev.orestegabo.sequo.ui.catalog

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
import org.jetbrains.compose.resources.painterResource
import sequo.shared.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource

@Composable
internal fun SequoShopCard(
    shop: SequoShop,
    selectedSubcategory: String? = null,
    onAddProduct: () -> Unit,
) {
    val visibleProducts = if (selectedSubcategory == null) {
        shop.products
    } else {
        shop.products.filter { productSubcategory(it) == selectedSubcategory }
    }
    val displayProducts = visibleProducts.take(6)
    val remainingProductCount = visibleProducts.size - displayProducts.size
    SequoCard {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                SequoIconMark(Icons.Filled.Storefront, MaterialTheme.colorScheme.primary, Modifier.size(52.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(shop.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${shop.area} / ${shop.kind}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.70f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(shop.openStatus, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                RatingMark(shop.rating)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetaPill(formatDistance(shop.distanceKm), SequoPrimary)
                MetaPill(formatCfa(baseDelivery(shop.distanceKm)), SequoSecondary)
                MetaPill(shop.eta, SequoAccent)
            }
            RuleRow(shop.photoStatus, shop.consolidation)
            displayProducts.forEach { product ->
                ProductLine(product = product, onAddProduct = onAddProduct)
            }
            if (remainingProductCount > 0) {
                Text(
                    "+$remainingProductCount more items in this shop",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
internal fun ShopSummaryRow(shop: SequoShop) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            SequoIconMark(Icons.Filled.Storefront, MaterialTheme.colorScheme.primary, Modifier.size(34.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(shop.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${shop.area} / ${shop.kind}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            RatingMark(shop.rating)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            MetaPill(formatDistance(shop.distanceKm), SequoPrimary)
            MetaPill(formatCfa(baseDelivery(shop.distanceKm)), SequoSecondary)
            MetaPill(shop.eta, SequoAccent)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            ShopStatusDot()
            Text(shop.openStatus, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(shop.consolidation, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
internal fun ShopStatusDot() {
    Box(
        modifier = Modifier
            .size(9.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
    )
}

@Composable
internal fun ProductLine(product: SequoProduct, onAddProduct: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                ProductImage(product = product, modifier = Modifier.size(58.dp))
                Column(Modifier.weight(1f)) {
                    Text(product.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(productSubcategory(product), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text(formatCfa(product.priceCfa), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                MetaPill(productSubcategory(product), if (product.bargainNote == null) SequoAccent else SequoSecondary)
                Text(product.optionHint, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                SequoTinyButton("Add", onAddProduct)
            }
            product.bargainNote?.let { note ->
                RuleRow("Negotiation", note)
            }
        }
    }
}

@Composable
internal fun CompactProductCard(shop: SequoShop, product: SequoProduct, onAddProduct: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.26f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProductImage(product = product, modifier = Modifier.size(52.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(product.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${shop.area} / ${formatCfa(product.priceCfa)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.64f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            SequoTinyButton("Add", onAddProduct)
        }
    }
}

@Composable
internal fun ProductImage(
    product: SequoProduct,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f)),
    ) {
        Image(
            painter = painterResource(productImageResource(product)),
            contentDescription = product.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    }
}

internal fun productImageResource(product: SequoProduct): DrawableResource {
    val text = "${product.name} ${product.detail} ${product.label}".lowercase()
    return when {
        "attieke" in text || "poisson" in text -> Res.drawable.sequo_food_grilled_fish_plate
        "riz gras" in text || "poulet" in text -> Res.drawable.sequo_food_koklo_meme
        "pizza" in text -> Res.drawable.sequo_food_pizza
        "dumpling" in text -> Res.drawable.sequo_food_dumplings
        "brochette" in text -> Res.drawable.sequo_food_brochettes
        "yassa" in text -> Res.drawable.sequo_food_yassa
        "akara" in text -> Res.drawable.sequo_food_akara
        "grill" in text -> Res.drawable.sequo_food_grill_platter
        "salmon" in text || "saumon" in text -> if ("box" in text || "paves" in text) Res.drawable.sequo_grocery_salmon_box else Res.drawable.sequo_grocery_salmon_fresh
        "tilapia" in text -> if ("barquette" in text || "pack" in text) Res.drawable.sequo_grocery_tilapia_pack else Res.drawable.sequo_grocery_tilapia_pile
        "poisson blanc" in text || "white fish" in text -> Res.drawable.sequo_grocery_fresh_fish
        "steak" in text -> Res.drawable.sequo_grocery_beef_steak
        "boeuf" in text || "beef" in text -> Res.drawable.sequo_grocery_beef_cuts
        "colza" in text || "rapeseed" in text -> when {
            "bio" in text -> Res.drawable.sequo_grocery_bio_colza_oil
            "1l" in text -> Res.drawable.sequo_grocery_colza_oil
            else -> Res.drawable.sequo_grocery_rapeseed_oil
        }
        "lait" in text || "milk" in text -> Res.drawable.sequo_grocery_fresh_milk
        "manioc" in text || "cassava" in text -> Res.drawable.sequo_grocery_cassava_flour
        "artisanale" in text || "artisan" in text -> Res.drawable.sequo_grocery_artisan_flour
        "farine" in text || "flour" in text -> Res.drawable.sequo_grocery_wheat_flour
        "vrac" in text && ("cajou" in text || "cashew" in text) -> Res.drawable.sequo_grocery_cashews_bulk
        "cajou" in text || "cashew" in text -> Res.drawable.sequo_grocery_cashews_bowl
        "noix de coco" in text || "coconut" in text -> Res.drawable.sequo_grocery_coconut
        "haricots melanges" in text || "mixed dry beans" in text || "beans mix" in text -> Res.drawable.sequo_grocery_beans_mix
        "soja" in text || "soybean" in text -> Res.drawable.sequo_grocery_soybeans
        "mais" in text || "corn" in text -> Res.drawable.sequo_grocery_corn
        "sorgho" in text || "sorghum" in text -> Res.drawable.sequo_grocery_sorghum
        "petits pois" in text || "peas" in text -> Res.drawable.sequo_grocery_peas
        "haricots" in text || "green beans" in text -> Res.drawable.sequo_grocery_green_beans
        "taro" in text -> Res.drawable.sequo_grocery_taro
        "patate" in text || "sweet potato" in text -> Res.drawable.sequo_grocery_sweet_potato
        "pomme de terre" in text || "potato" in text -> Res.drawable.sequo_grocery_potatoes
        "carotte" in text || "carrot" in text -> Res.drawable.sequo_grocery_carrots
        "poivron" in text || "green pepper" in text -> Res.drawable.sequo_grocery_green_pepper
        "aubergine" in text || "eggplant" in text -> Res.drawable.sequo_grocery_eggplant
        "pagne" in text -> Res.drawable.sequo_fashion_floral_shorts
        "banana" in text -> Res.drawable.sequo_grocery_corn
        "cherry" in text -> Res.drawable.sequo_grocery_tomatoes_cluster
        "fruit" in text -> Res.drawable.sequo_grocery_tomatoes_vine
        "tomates" in text || "tomato" in text -> if ("grappe" in text || "vine" in text) Res.drawable.sequo_grocery_tomatoes_vine else Res.drawable.sequo_grocery_tomatoes_cluster
        "onion" in text || "fresh" in text -> Res.drawable.sequo_grocery_tomatoes_cluster
        "iphone" in text && "rose" in text -> Res.drawable.sequo_electronics_iphone_pink
        "iphone" in text -> Res.drawable.sequo_electronics_iphone_black
        "poco" in text || "tecno" in text || "phone" in text -> Res.drawable.sequo_electronics_poco_phone
        "dell" in text || "latitude" in text -> Res.drawable.sequo_electronics_laptop_pro
        "hp" in text || "laptop" in text -> Res.drawable.sequo_electronics_laptop_silver
        "charger" in text -> Res.drawable.sequo_electronics_poco_phone
        "hydrafizz" in text || "hydration" in text -> Res.drawable.sequo_pharmacy_hydrafizz
        "booster" in text -> Res.drawable.sequo_pharmacy_upsa_booster
        "vitamine" in text || "vitamin" in text -> Res.drawable.sequo_pharmacy_vitamin_c
        "paracetamol" in text -> Res.drawable.sequo_pharmacy_paracetamol
        "doliprane" in text -> Res.drawable.sequo_pharmacy_doliprane
        "pampers active" in text -> Res.drawable.sequo_baby_pampers_active_jpeg
        "pampers" in text -> Res.drawable.sequo_baby_pampers_newborn
        "toilet paper soft" in text || "papier toilette soft" in text -> Res.drawable.sequo_home_toilet_paper_soft
        "toilet paper classic" in text || "papier toilette classic" in text -> Res.drawable.sequo_home_toilet_paper_classic
        "bamboo" in text || "bambou" in text -> Res.drawable.sequo_home_toilet_paper_bamboo
        "campus" in text -> Res.drawable.sequo_fashion_green_sneaker
        "white running" in text -> Res.drawable.sequo_fashion_white_runner
        "monogram" in text -> Res.drawable.sequo_fashion_monogram_sneaker
        "slip-on" in text || "slipon" in text -> Res.drawable.sequo_fashion_canvas_slipon
        "tan low" in text -> Res.drawable.sequo_fashion_tan_sneaker
        "brown comfort" in text -> Res.drawable.sequo_fashion_brown_shoe
        "black leather sneaker" in text -> Res.drawable.sequo_fashion_black_sneaker
        "gray casual" in text -> Res.drawable.sequo_fashion_gray_sneaker
        "navy dress socks" in text -> Res.drawable.sequo_fashion_socks_navy
        "three-color socks" in text -> Res.drawable.sequo_fashion_socks_three_pack
        "office sock" in text -> Res.drawable.sequo_fashion_socks_mixed_pack
        "blue argyle socks" in text -> Res.drawable.sequo_fashion_socks_blue_argyle
        "black argyle socks" in text -> Res.drawable.sequo_fashion_socks_black_argyle
        "wool knit socks" in text -> Res.drawable.sequo_fashion_socks_wool
        "sport sock pack" in text -> Res.drawable.sequo_fashion_socks_sport_pack
        "white crew socks" in text -> Res.drawable.sequo_fashion_socks_white
        "brown striped socks" in text -> Res.drawable.sequo_fashion_socks_striped_brown
        "orange ribbed socks" in text -> Res.drawable.sequo_fashion_socks_orange
        "graphic black" in text -> Res.drawable.sequo_fashion_graphic_tee
        "mountain" in text -> Res.drawable.sequo_fashion_white_graphic_tee
        "plain black" in text -> Res.drawable.sequo_fashion_black_tee
        "plain white" in text -> Res.drawable.sequo_fashion_white_tee
        "green tee" in text -> Res.drawable.sequo_fashion_green_tee
        "blue office" in text -> Res.drawable.sequo_fashion_blue_shirt
        "striped shirt" in text -> Res.drawable.sequo_fashion_striped_shirt
        "red chino" in text -> Res.drawable.sequo_fashion_red_shorts
        "rose chino" in text -> Res.drawable.sequo_fashion_pink_shorts
        "floral shorts" in text -> Res.drawable.sequo_fashion_floral_shorts
        "denim shorts" in text -> Res.drawable.sequo_fashion_denim_shorts
        "blue straight" in text -> Res.drawable.sequo_fashion_blue_jeans
        "dark denim" in text -> Res.drawable.sequo_fashion_dark_jeans
        "light relaxed" in text -> Res.drawable.sequo_fashion_light_jeans
        "saint germain" in text -> Res.drawable.sequo_beauty_vanilla_perfume
        "blue heel" in text -> Res.drawable.sequo_beauty_blue_heel_perfume
        "dior homme" in text -> Res.drawable.sequo_beauty_dior_homme
        "sauvage" in text -> Res.drawable.sequo_beauty_sauvage
        "interdit" in text -> Res.drawable.sequo_beauty_red_perfume
        "nuxe" in text -> Res.drawable.sequo_beauty_nuxe_perfume
        "scandal" in text -> Res.drawable.sequo_beauty_scandal_perfume
        "aqua" in text -> Res.drawable.sequo_beauty_aqua_perfume
        "opera" in text -> Res.drawable.sequo_beauty_opera_perfume
        "coconut" in text -> Res.drawable.sequo_beauty_coconut_perfume
        "ace beaute" in text -> Res.drawable.sequo_beauty_concealer
        "lip palette" in text -> Res.drawable.sequo_beauty_lip_palette
        "gold concealer" in text -> Res.drawable.sequo_beauty_gold_concealer
        "nars" in text -> Res.drawable.sequo_beauty_nars_sticks
        "lifter" in text -> Res.drawable.sequo_beauty_lifter_glaze
        "liquid blush" in text -> Res.drawable.sequo_beauty_liquid_blush
        "chronograph" in text -> Res.drawable.sequo_accessory_chrono_watch
        "smart watch" in text -> Res.drawable.sequo_accessory_smart_watch
        "black quartz" in text -> Res.drawable.sequo_accessory_black_watch
        "casio" in text -> Res.drawable.sequo_accessory_casio_blue
        "manchester blue" in text -> Res.drawable.sequo_sports_manchester_blue
        "manchester light" in text -> Res.drawable.sequo_sports_manchester_light_blue
        "manchester black" in text -> Res.drawable.sequo_sports_manchester_black
        "chelsea blue" in text -> Res.drawable.sequo_sports_chelsea_blue
        "arsenal red" in text -> Res.drawable.sequo_sports_arsenal_red
        "arsenal yellow" in text -> Res.drawable.sequo_sports_arsenal_yellow
        "togo yellow" in text -> Res.drawable.sequo_sports_togo_yellow
        "japan volleyball" in text -> Res.drawable.sequo_sports_japan_volleyball
        "bal green" in text -> Res.drawable.sequo_sports_basket_green
        "bal black" in text -> Res.drawable.sequo_sports_basket_black
        "lakers" in text -> Res.drawable.sequo_sports_lakers_yellow
        "bulls" in text -> Res.drawable.sequo_sports_bulls_red
        "purple court" in text -> Res.drawable.sequo_sports_purple_basket_shoe
        "pink court" in text -> Res.drawable.sequo_sports_pink_basket_shoe
        "black court" in text -> Res.drawable.sequo_sports_black_basket_shoe
        "fifa football" in text -> Res.drawable.sequo_sports_football
        "volleyball" in text -> Res.drawable.sequo_sports_volleyball
        "american football" in text -> Res.drawable.sequo_sports_american_football
        "mir vaisselle" in text -> Res.drawable.sequo_home_dish_soap_mir
        "omo washing" in text -> Res.drawable.sequo_home_laundry_omo
        "ajax lavender" in text -> Res.drawable.sequo_home_floor_cleaner_ajax_lavender
        "sanytol disinfectant" in text -> Res.drawable.sequo_home_disinfectant_sanytol
        "ajax window" in text -> Res.drawable.sequo_home_window_cleaner_ajax
        "bathroom" in text -> Res.drawable.sequo_home_bathroom_sanytol
        "harpic" in text -> Res.drawable.sequo_home_toilet_cleaner_harpic
        "lot de 4" in text || "set of 4" in text -> Res.drawable.sequo_auto_tires_stack
        "pneu" in text || "tire" in text -> Res.drawable.sequo_auto_tire
        "roulement" in text || "bearing" in text -> Res.drawable.sequo_auto_bearing
        "demarreur" in text || "starter" in text -> Res.drawable.sequo_auto_starter
        "carrosserie" in text || "body" in text || "door" in text -> Res.drawable.sequo_auto_body_parts
        "water" in text || "eau" in text -> Res.drawable.sequo_home_dish_soap_mir
        "lampe" in text || "led" in text -> Res.drawable.sequo_home_window_cleaner_ajax
        "multiprise" in text || "socket" in text -> Res.drawable.sequo_electronics_laptop_silver
        "rangement" in text || "storage" in text -> Res.drawable.sequo_home_toilet_paper_bamboo
        "savon" in text || "papier" in text || "lessive" in text -> Res.drawable.sequo_home_laundry_omo
        else -> Res.drawable.sequo_grocery_tomatoes_cluster
    }
}
