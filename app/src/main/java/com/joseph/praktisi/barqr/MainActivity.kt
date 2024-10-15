package com.joseph.praktisi.barqr

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.joseph.praktisi.barqr.components.CameraScreen
import com.joseph.praktisi.barqr.models.CameraScreenUiEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            handlePermissionsResult(permissions)
        }

    private val viewModel by viewModels<MainViewModel>()
    private lateinit var imageCapture: ImageCapture

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        observeCameraEvents()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            CameraScreen(uiState, onEventReceiver = viewModel)
        }
    }

    private fun observeCameraEvents() {
        lifecycleScope.launch(Dispatchers.Main.immediate) {
            viewModel.uiEvent.flowWithLifecycle(lifecycle).collect { event ->
                handleCameraScreenEvent(event)
            }
        }
    }

    private fun handlePermissionsResult(permissions: Map<String, Boolean>) {
        val isPermissionGranted = permissions.all { it.value }

        if (!isPermissionGranted) {
            Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show()
        } else {
            startCamera()
        }
    }

    private fun handleCameraScreenEvent(event: CameraScreenUiEvent) {
        when (event) {
            CameraScreenUiEvent.RequestPermission -> requestPermissions()
            CameraScreenUiEvent.TakePicture -> takePhoto()
            else -> Unit
        }
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                bindCamera(cameraProvider)
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to start camera: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCamera(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder().build()
        imageCapture = ImageCapture.Builder().build()
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture, preview)
            viewModel.onEvent(CameraScreenUiEvent.OnPreviewReady(preview))
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to bind camera: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun takePhoto() {
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)
                    viewModel.onEvent(CameraScreenUiEvent.OnPictureTaken(image))
                }
            }
        )
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }
}
