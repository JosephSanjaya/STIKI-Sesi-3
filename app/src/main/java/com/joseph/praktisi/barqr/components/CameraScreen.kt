package com.joseph.praktisi.barqr.components

import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.joseph.praktisi.barqr.models.CameraScreenUiEvent
import com.joseph.praktisi.barqr.models.CameraScreenUiState
import com.joseph.praktisi.utils.OnEventReceiver

@Composable
fun CameraScreen(
    uiState: CameraScreenUiState,
    modifier: Modifier = Modifier,
    onEventReceiver: OnEventReceiver<CameraScreenUiEvent> = OnEventReceiver { }
) {
    Scaffold(modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            AndroidView(factory = { ctx ->
                PreviewView(ctx)
            }, update = { view ->
                uiState.preview?.surfaceProvider = view.surfaceProvider
            })

            Button(
                onClick = {
                    onEventReceiver.onEvent(CameraScreenUiEvent.TakePicture)
                }, modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Text(text = "Take Picture")
            }

            AnimatedVisibility(
                visible = uiState.imageTaken != null,
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.TopEnd)
            ) {
                Image(
                    bitmap = uiState.imageTaken!!.asImageBitmap(),
                    contentDescription = "Image Taken",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            }

            AnimatedVisibility(
                visible = uiState.isLoading,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Preview
@Composable
fun PreviewCameraScreen() {
    CameraScreen(uiState = CameraScreenUiState(isLoading = true))
}
