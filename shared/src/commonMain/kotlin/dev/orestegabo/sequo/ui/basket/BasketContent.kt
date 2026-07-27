package dev.orestegabo.sequo.ui.basket

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

@Composable
internal fun BasketContent(extraBasketItems: Int) {
    var selectedPayment by remember { mutableStateOf("Yas Togo") }

    SequoAppBar(
        title = "Checkout",
        subtitle = "Basket locked before payment",
        leadingIcon = Icons.Filled.ShoppingBasket,
        actions = listOf(
            AppBarAction(Icons.Filled.Lock, "Secure checkout", emphasized = true),
            AppBarAction(Icons.Filled.Payments, "Payment methods"),
        ),
    )
    SequoStatusStrip(
        icon = Icons.Filled.Lock,
        title = "Payment validation required",
        detail = "Order completion unlocks only after Yas Togo or Moov Africa confirms.",
        tag = "secure",
    )
    DeliveryAddressCard()
    SequoSectionCard(title = "Basket", action = "${sequoBasket.sumOf { it.quantity } + extraBasketItems} items") {
        sequoBasket.forEach { entry ->
            BasketLine(entry)
        }
        if (extraBasketItems > 0) {
            BasketAddedLine(extraBasketItems)
        }
    }
    SequoSectionCard(title = "Food options", action = "custom") {
        RuleRow("Attieke poisson braise", sequoShops[0].products[0].optionHint)
        RuleRow("No mixing with goods", "Hot food travels in a separate sealed bag inside the Sequo package.")
    }
    SequoSectionCard(title = "Consolidation", action = "Lome route") {
        RuleRow("Assigame + Akodessewa", "Eligible for one Sequo package after inspection at pickup.")
        RuleRow("Food exception", "Tokoin hot meal keeps its own thermal seal and pickup timing.")
    }
    SequoCheckoutCard(extraBasketItems, selectedPayment, onPaymentSelected = { selectedPayment = it })
}
