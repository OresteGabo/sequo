package dev.orestegabo.sequo.core.platform

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import sequo.shared.generated.resources.Res
import sequo.shared.generated.resources.photo_export_cancelled
import sequo.shared.generated.resources.photo_export_in_progress
import sequo.shared.generated.resources.photo_export_opening_share
import sequo.shared.generated.resources.photo_export_prepare_failed
import sequo.shared.generated.resources.photo_export_save_failed
import sequo.shared.generated.resources.photo_export_saved
import sequo.shared.generated.resources.photo_export_share_failed

@Composable
internal actual fun rememberProductPhotoExporter(): ProductPhotoExporter {
    val context = LocalContext.current.applicationContext
    val saver = rememberAndroidDocumentSaver("image/jpeg")
    return remember(context, saver) { AndroidProductPhotoExporter(context, saver) }
}

private class AndroidProductPhotoExporter(
    private val context: Context,
    private val saver: AndroidDocumentSaver,
) : ProductPhotoExporter {
    override suspend fun saveWatermarkedPhoto(
        image: ImageBitmap,
        fileName: String,
        productName: String,
    ): ProductPhotoExportResult = try {
        val bytes = withContext(Dispatchers.IO) {
            val watermarked = image.asAndroidBitmap().withSequoWatermark(productName)
            try { watermarked.jpegBytes() } finally { watermarked.recycle() }
        }
        when (saver.save(fileName.safeImageFileName(), bytes)) {
            DocumentSaveResult.Saved -> ProductPhotoExportResult(true, Res.string.photo_export_saved)
            DocumentSaveResult.Cancelled -> ProductPhotoExportResult(false, Res.string.photo_export_cancelled)
            DocumentSaveResult.Failed -> ProductPhotoExportResult(false, Res.string.photo_export_save_failed)
            DocumentSaveResult.Busy -> ProductPhotoExportResult(false, Res.string.photo_export_in_progress)
        }
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        ProductPhotoExportResult(false, Res.string.photo_export_prepare_failed)
    }

    override suspend fun sharePhoto(
        image: ImageBitmap,
        fileName: String,
        productName: String,
    ): ProductPhotoExportResult = try {
        val file = withContext(Dispatchers.IO) {
            val directory = File(context.cacheDir, "shared-product-photos").apply {
                check(isDirectory || mkdirs())
            }
            File.createTempFile("sequo-photo-", ".jpg", directory).also { target ->
                target.outputStream().use { output ->
                    check(image.asAndroidBitmap().compress(Bitmap.CompressFormat.JPEG, 94, output))
                }
            }
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.sharedfiles", file)
        val share = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TITLE, productName)
            clipData = ClipData.newRawUri(fileName.safeImageFileName(), uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(share, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        ProductPhotoExportResult(true, Res.string.photo_export_opening_share)
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        ProductPhotoExportResult(false, Res.string.photo_export_share_failed)
    }
}

private fun Bitmap.jpegBytes(): ByteArray = ByteArrayOutputStream().use { output ->
    check(compress(Bitmap.CompressFormat.JPEG, 94, output))
    output.toByteArray()
}

private fun Bitmap.withSequoWatermark(productName: String): Bitmap {
    val output = copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(output)
    val density = width.coerceAtLeast(height) / 900f
    val padding = 18f * density
    val corner = 18f * density
    val logoSize = 34f * density
    val watermarkTitleSize = 18f * density
    val label = "Sequo"
    val subtitle = productName.take(34)

    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = watermarkTitleSize
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(220, 255, 255, 255)
        textSize = 12f * density
    }
    val titleWidth = titlePaint.measureText(label)
    val subtitleWidth = subtitlePaint.measureText(subtitle)
    val cardWidth = padding * 3 + logoSize + maxOf(titleWidth, subtitleWidth)
    val cardHeight = padding * 2 + logoSize
    val left = width - cardWidth - padding
    val top = height - cardHeight - padding
    val card = RectF(left, top, left + cardWidth, top + cardHeight)

    Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(168, 9, 23, 18)
        canvas.drawRoundRect(card, corner, corner, this)
    }
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(61, 184, 130)
        canvas.drawCircle(left + padding + logoSize / 2, top + padding + logoSize / 2, logoSize / 2, this)
    }
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 3.4f * density
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        val cx = left + padding + logoSize / 2
        val cy = top + padding + logoSize / 2
        canvas.drawLine(cx - 8f * density, cy + 3f * density, cx - 1f * density, cy + 10f * density, this)
        canvas.drawLine(cx - 1f * density, cy + 10f * density, cx + 10f * density, cy - 9f * density, this)
    }
    val textLeft = left + padding * 2 + logoSize
    canvas.drawText(label, textLeft, top + padding + 15f * density, titlePaint)
    canvas.drawText(subtitle, textLeft, top + padding + 32f * density, subtitlePaint)
    return output
}

private fun String.safeImageFileName(): String {
    val cleaned = replace(Regex("[^A-Za-z0-9._-]"), "_").ifBlank { "sequo-photo" }
    return if (cleaned.endsWith(".jpg", ignoreCase = true) || cleaned.endsWith(".jpeg", ignoreCase = true)) {
        cleaned
    } else {
        "$cleaned.jpg"
    }
}
