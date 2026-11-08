package dev.orestegabo.sequo.ui.catalog

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import dev.orestegabo.sequo.core.network.NetworkConfig
import dev.orestegabo.sequo.core.network.createSequoHttpClient
import dev.orestegabo.sequo.model.SequoProduct
import io.ktor.client.request.accept
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.CancellationException
import kotlinx.io.readByteArray
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.painterResource

private const val MaxImageBytes = 8 * 1024 * 1024
private val catalogImages by lazy { createSequoHttpClient() }

@Composable
internal fun CatalogProductImage(product: SequoProduct, modifier: Modifier, contentScale: ContentScale) {
    val source = product.imageUrl
    val image = remember(source) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(source) {
        val url = when {
            source == "/api/catalog/products/${product.id}/image" ->
                NetworkConfig.BaseUrl.trimEnd('/') + source
            source?.startsWith("https://") == true -> source
            else -> return@LaunchedEffect
        }
        try {
            image.value = catalogImages.prepareGet(url) {
                accept(ContentType.Image.Any)
            }.execute { response ->
                val bytes = response.bodyAsChannel().readRemaining(MaxImageBytes.toLong() + 1).readByteArray()
                if (bytes.size in 1..MaxImageBytes) bytes.decodeToImageBitmap() else null
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            // Keep the existing catalog illustration if the photo is unavailable.
        }
    }
    val bitmap = image.value
    if (bitmap != null) {
        Image(bitmap, product.name, modifier, contentScale = contentScale)
    } else {
        Image(painterResource(productImageResource(product)), product.name, modifier, contentScale = contentScale)
    }
}
