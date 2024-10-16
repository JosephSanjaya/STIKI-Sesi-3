package com.joseph.praktisi.barqr

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.joseph.praktisi.barqr.components.CameraScreen
import com.joseph.praktisi.barqr.models.CameraScreenUiEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            handlePermissionsResult(permissions)
        }

    private val viewModel by viewModels<MainViewModel>()
    private lateinit var imageCapture: ImageCapture
    private val scannedBarcodes = mutableSetOf<String>()

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
        val analysisExecutor = Executors.newSingleThreadExecutor()
        try {
            val imageAnalysis = buildImageAnalysis()
            setImageAnalyzer(imageAnalysis, analysisExecutor)
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                imageCapture,
                preview,
                imageAnalysis
            )
            viewModel.onEvent(CameraScreenUiEvent.OnPreviewReady(preview))
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to bind camera: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun buildImageAnalysis(): ImageAnalysis {
        val resolutionSelector = ResolutionSelector.Builder()
            .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
            .setResolutionFilter { supportedSizes, _ ->
                supportedSizes.filter { it.width == 1280 && it.height == 720 }
            }
            .build()

        return ImageAnalysis.Builder()
            .setResolutionSelector(resolutionSelector)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
            .build()
    }

    @OptIn(ExperimentalGetImage::class)
    private fun setImageAnalyzer(imageAnalysis: ImageAnalysis, analysisExecutor: Executor) {
        var lastAnalyzedTime = 0L
        val frameInterval = 500L  // Process one frame every 500ms

        imageAnalysis.setAnalyzer(analysisExecutor) { imageProxy ->
            val currentTime = System.currentTimeMillis()

            /**
             * This ensures that a new frame is processed only after the specified time interval (500ms) has passed since the last analyzed frame.
             * If the interval has not passed, the imageProxy is closed early, and no analysis is performed on that frame.
             */
            if (currentTime - lastAnalyzedTime >= frameInterval) {
                lastAnalyzedTime = currentTime

                val mediaImage = imageProxy.image ?: return@setAnalyzer
                val image =
                    InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                val scanner = BarcodeScanning.getClient()

                scanner.process(image)
                    .addOnCompleteListener { result ->
                        if (result.isSuccessful) {
                            val barcodes = result.result
                            viewModel.onEvent(CameraScreenUiEvent.OnBarcodeScanned(barcodes))
                        }
                        imageProxy.close()  // Always close the imageProxy to free resources
                    }
            } else {
                imageProxy.close()  // Close imageProxy if frame is skipped
            }
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
