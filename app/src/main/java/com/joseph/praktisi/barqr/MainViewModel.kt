package com.joseph.praktisi.barqr

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.joseph.praktisi.barqr.models.CameraScreenUiEvent
import com.joseph.praktisi.barqr.models.CameraScreenUiState
import com.joseph.praktisi.utils.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel :
    BaseViewModel<CameraScreenUiState, CameraScreenUiEvent>(CameraScreenUiState()) {

    override fun onUiStateSubscribed() {
        super.onUiStateSubscribed()
        onEvent(CameraScreenUiEvent.RequestPermission)
    }

    override fun onEventSideEffect(event: CameraScreenUiEvent) {
        super.onEventSideEffect(event)
        when (event) {
            is CameraScreenUiEvent.OnPreviewReady -> {
                _uiState.update { it.copy(preview = event.preview) }
            }

            is CameraScreenUiEvent.OnPictureTaken -> onPictureTakenEvent(event)

            is CameraScreenUiEvent.OnBarcodeScanned -> _uiState.update { it.copy(barcodes = event.barcodes) }

            else -> Unit
        }
    }

    private fun onPictureTakenEvent(event: CameraScreenUiEvent.OnPictureTaken) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            val image = event.picture
            try {
                val bitmap = image.toBitmap()
                _uiState.update { it.copy(imageTaken = bitmap, isLoading = false) }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing image: ${e.message}")
                _uiState.update { it.copy(isLoading = false) }
            } finally {
                image.close()
            }
        }
    }
}
