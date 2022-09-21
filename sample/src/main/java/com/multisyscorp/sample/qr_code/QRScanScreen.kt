@file:Suppress("OPT_IN_IS_NOT_ENABLED", "unused")

package com.multisyscorp.sample.qr_code

import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.multisyscorp.multi.helpers.QRCodeHelper

const val TAG = "QRScanScreen"

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun QRScanContent() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var overrideBack by remember { mutableStateOf(false) }
    var hasCameraPermission by remember { mutableStateOf(false) }
    var qrCodeRawValue by remember { mutableStateOf("") }

    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )

    when (cameraPermissionState.status) {
        PermissionStatus.Granted -> {
            hasCameraPermission = true
        }
        is PermissionStatus.Denied -> {
            Column {
                val textToShow = when {
                    cameraPermissionState.status.shouldShowRationale -> {
                        // If the user has denied the permission but the rationale can be shown,
                        // then gently explain why the app requires this permission
                        "The camera is important for this app. Please grant the permission."
                    }
                    else -> {
                        // If it's the first time the user lands on this feature, or the user
                        // doesn't want to be asked again for this permission, explain that the
                        // permission is required
                        "Camera permission required for this feature to be available. Please grant the permission"
                    }
                }
                Text(textToShow)
                Button(
                    onClick = {
                        cameraPermissionState.launchPermissionRequest()
                    }
                ) {
                    Text("Request permission")
                }
            }
        }
    }

    val selectImageFromGalleryResult = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { fileUri ->
                val inputStream = context.contentResolver.openInputStream(fileUri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                QRCodeHelper.decodeBitmap(
                    context = context,
                    bitmap = bitmap,
                    onResult = { result ->
                        qrCodeRawValue = result ?: ""
                    },
                    onError = {
                        Log.d(TAG, "Detector Error")
                    },
                    onNotFound = {
                        Log.d(TAG, "Not Found")
                    },
                    onException = {
                        Log.d(TAG, "Exception")
                    }
                )
            }
        }
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = {
                when {
                    !hasCameraPermission -> {
                        cameraPermissionState.launchPermissionRequest()
                    }
                }
            }
        ) {
            Text(text = "Scan QR")
        }
        Spacer(modifier = Modifier.height(height = 24.dp))
        Button(
            onClick = {
                selectImageFromGalleryResult.launch("image/*")
            }
        ) {
            Text(text = "Select QR From Gallery/Photos")
        }
        Spacer(modifier = Modifier.height(height = 24.dp))
        Text(
            text = "Result:\n$qrCodeRawValue",
            modifier = Modifier
                .padding(
                    start = 32.dp,
                    end = 32.dp
                ),
            textAlign = TextAlign.Center
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            hasCameraPermission -> {
                AndroidView(
                    factory = { context ->
                        val previewView = PreviewView(context)
                        val preview = Preview.Builder().build()
                        val selector = CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                            .build()
                        preview.setSurfaceProvider(previewView.surfaceProvider)
                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                        imageAnalysis.setAnalyzer(
                            ContextCompat.getMainExecutor(context),
                            QRCodeAnalyzer(
                                supportedImageFormats = listOf(
                                    ImageFormat.YUV_420_888,
                                    ImageFormat.YUV_422_888,
                                    ImageFormat.YUV_444_888,
                                ),
                                onQRCodeScanned = { result ->
                                    qrCodeRawValue = result ?: ""
                                    overrideBack = true
                                },
                                onException = {
                                    Log.d(TAG, "Exception")
                                }
                            )
                        )

                        try {
                            cameraProviderFuture.get().bindToLifecycle(
                                lifecycleOwner,
                                selector,
                                preview,
                                imageAnalysis
                            )
                        } catch (_: Exception) {
                            Log.d(TAG, "Exception")
                        }

                        return@AndroidView previewView
                    },
                    modifier = Modifier.weight(weight = 1f)
                )
            }
        }
    }

    BackHandler {
        overrideBack = true
    }

    when {
        overrideBack -> {
            overrideBack = false
            hasCameraPermission = false
            cameraProviderFuture.get().unbindAll()
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun QRScanContentPreview() {
    MaterialTheme {
        QRScanContent()
    }
}