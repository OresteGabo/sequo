package dev.orestegabo.sequo.core.platform

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import sequo.shared.generated.resources.Res
import sequo.shared.generated.resources.photo_export_opening_share
import sequo.shared.generated.resources.photo_export_prepare_failed
import sequo.shared.generated.resources.photo_export_save_failed
import sequo.shared.generated.resources.photo_export_saved
import sequo.shared.generated.resources.photo_export_share_failed

@Composable
internal actual fun rememberProductPhotoExporter(): ProductPhotoExporter {
    val context = LocalContext.current
    return remember(context) { AndroidProductPhotoExporter(context.applicationContext) }
}

private class AndroidProductPhotoExporter(
    private val context: Context,
) : ProductPhotoExporter {
    override suspend fun saveWatermarkedPhoto(
        image: ImageBitmap,
        fileName: String,
        productName: String,
    ): ProductPhotoExportResult = runCatching {
        val bitmap = image.asAndroidBitmap()
        val watermarked = bitmap.withSequoWatermark(productName)
        val safeName = fileName.safeImageFileName()
        context.writeWatermarkedImage(watermarked, safeName)
            ?: return ProductPhotoExportResult(false, Res.string.photo_export_save_failed)
        ProductPhotoExportResult(true, Res.string.photo_export_saved)
    }.getOrElse {
        ProductPhotoExportResult(false, Res.string.photo_export_save_failed)
    }

    override suspend fun sharePhoto(
        image: ImageBitmap,
        fileName: String,
        productName: String,
    ): ProductPhotoExportResult = runCatching {
        val bitmap = image.asAndroidBitmap()
        val watermarked = bitmap.withSequoWatermark(productName)
        val safeName = fileName.safeImageFileName()
        val uri = context.writeWatermarkedImage(watermarked, safeName)
            ?: return ProductPhotoExportResult(false, Res.string.photo_export_prepare_failed)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Shared from Sequo")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share photo").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        ProductPhotoExportResult(true, Res.string.photo_export_opening_share)
    }.getOrElse {
        ProductPhotoExportResult(false, Res.string.photo_export_share_failed)
    }
}

private fun Context.writeWatermarkedImage(
    bitmap: Bitmap,
    safeName: String,
): Uri? {
    val resolver = contentResolver
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, safeName)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Sequo")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    }
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return null
    resolver.openOutputStream(uri)?.use { output ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 94, output)
    } ?: return null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
    }
    return uri
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
