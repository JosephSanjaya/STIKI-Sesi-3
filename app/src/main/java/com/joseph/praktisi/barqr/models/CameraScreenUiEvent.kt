package com.joseph.praktisi.barqr.models

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview

sealed interface CameraScreenUiEvent {
    data object RequestPermission : CameraScreenUiEvent
    data class OnPreviewReady(val preview: Preview) : CameraScreenUiEvent
    data object TakePicture : CameraScreenUiEvent
    data class OnPictureTaken(val picture: ImageProxy) : CameraScreenUiEvent
}
