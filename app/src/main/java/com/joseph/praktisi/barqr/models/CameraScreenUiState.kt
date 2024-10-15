package com.joseph.praktisi.barqr.models

import android.graphics.Bitmap
import androidx.camera.core.Preview
import androidx.compose.runtime.Stable

@Stable
data class CameraScreenUiState(
    val isLoading: Boolean = false,
    val preview: Preview? = null,
    val imageTaken: Bitmap? = null
)
