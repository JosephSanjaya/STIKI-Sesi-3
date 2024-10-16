package com.joseph.praktisi.barqr.models

import android.graphics.Bitmap
import androidx.camera.core.Preview
import androidx.compose.runtime.Stable
import com.google.mlkit.vision.barcode.common.Barcode

@Stable
data class CameraScreenUiState(
    val isLoading: Boolean = false,
    val preview: Preview? = null,
    val imageTaken: Bitmap? = null,
    val barcodes: List<Barcode> = emptyList()
)
