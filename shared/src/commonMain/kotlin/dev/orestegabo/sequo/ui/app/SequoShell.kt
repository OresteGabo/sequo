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
import org.jetbrains.compose.resources.painterResource
import sequo.shared.generated.resources.*

@Composable
internal fun SequoShell() {
    var currentDestination by remember { mutableStateOf(SequoSection.Home) }
    var extraBasketItems by remember { mutableStateOf(0) }
    val basketCount = sequoBasket.sumOf { it.quantity } + extraBasketItems

    Box(modifier = Modifier.fillMaxSize()) {
        SequoAmbientBackground(modifier = Modifier.fillMaxSize())
        SequoContentStage(
            currentDestination = currentDestination,
            onDestinationSelected = { currentDestination = it },
            extraBasketItems = extraBasketItems,
            onAddProduct = { extraBasketItems += 1 },
            modifier = Modifier.fillMaxSize(),
        )
        SequoBottomBar(
            currentDestination = currentDestination,
            onDestinationSelected = { currentDestination = it },
            pendingBasketCount = basketCount,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
internal fun SequoContentStage(
    currentDestination: SequoSection,
    onDestinationSelected: (SequoSection) -> Unit,
    extraBasketItems: Int,
    onAddProduct: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SequoScreenColumn(modifier = modifier) {
        when (currentDestination) {
            SequoSection.Home -> HomeContent(onDestinationSelected, onAddProduct)
            SequoSection.Markets -> MarketsContent(onAddProduct)
            SequoSection.Basket -> BasketContent(extraBasketItems)
            SequoSection.Orders -> OrdersContent()
            SequoSection.Account -> AccountContent()
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
            .padding(start = 20.dp, top = 26.dp, end = 20.dp, bottom = 126.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content,
    )
}
