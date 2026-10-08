package dev.orestegabo.sequo.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

internal data class ProductPhotoExportResult(
    val success: Boolean,
    val message: String,
)

internal interface ProductPhotoExporter {
    suspend fun saveWatermarkedPhoto(
        image: ImageBitmap,
        fileName: String,
        productName: String,
    ): ProductPhotoExportResult

    suspend fun sharePhoto(
        image: ImageBitmap,
        fileName: String,
        productName: String,
    ): ProductPhotoExportResult
}

@Composable
internal expect fun rememberProductPhotoExporter(): ProductPhotoExporter
