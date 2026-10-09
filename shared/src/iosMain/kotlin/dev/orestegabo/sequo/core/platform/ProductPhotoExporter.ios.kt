package dev.orestegabo.sequo.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import sequo.shared.generated.resources.Res
import sequo.shared.generated.resources.photo_export_ios_save_pending
import sequo.shared.generated.resources.photo_export_ios_share_pending

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
            message = Res.string.photo_export_ios_save_pending,
        )

    override suspend fun sharePhoto(
        image: ImageBitmap,
        fileName: String,
        productName: String,
    ): ProductPhotoExportResult =
        ProductPhotoExportResult(
            success = false,
            message = Res.string.photo_export_ios_share_pending,
        )
}
