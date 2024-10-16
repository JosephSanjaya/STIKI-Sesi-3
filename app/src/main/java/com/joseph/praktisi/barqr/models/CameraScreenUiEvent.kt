package com.joseph.praktisi.barqr.models

import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import com.google.mlkit.vision.barcode.common.Barcode

sealed interface CameraScreenUiEvent {
    data object RequestPermission : CameraScreenUiEvent
    data class OnPreviewReady(val preview: Preview) : CameraScreenUiEvent
    data object TakePicture : CameraScreenUiEvent
    data class OnPictureTaken(val picture: ImageProxy) : CameraScreenUiEvent
    data class OnBarcodeScanned(val barcodes: List<Barcode>) : CameraScreenUiEvent
}
