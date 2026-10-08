package dev.orestegabo.sequo.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap

@Composable
internal actual fun rememberProductPhotoExporter(): ProductPhotoExporter =
    remember { IosProductPhotoExporter() }

private class IosProductPhotoExporter : ProductPhotoExporter {
    override suspend fun saveWatermarkedPhoto(
        image: ImageBitmap,
        fileName: String,
        productName: String,
    ): ProductPhotoExportResult =
        ProductPhotoExportResult(
            success = false,
            message = "Photo saving is ready in the menu; iOS needs the native Photos exporter pass next.",
        )

    override suspend fun sharePhoto(
        image: ImageBitmap,
        fileName: String,
        productName: String,
    ): ProductPhotoExportResult =
        ProductPhotoExportResult(
            success = false,
            message = "Sharing is ready in the menu; iOS needs the native share exporter pass next.",
        )
}
