package id.rancak.app.presentation.barcode

import android.Manifest
import android.annotation.SuppressLint
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

/**
 * Android barcode scanner using CameraX + ML Kit.
 *
 * Requests CAMERA permission at runtime if not already granted.
 * Uses ImageAnalysis with ML Kit BarcodeScanning for real-time detection.
 * Debounces rapid repeated callbacks for the same barcode (1 second window).
 */
@Composable
actual fun BarcodeScannerView(
    onBarcodeDetected: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current

    // Check initial permission state
    val initialGranted = remember {
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    var cameraPermissionGranted by remember { mutableStateOf(initialGranted) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        cameraPermissionGranted = granted
    }

    // Request permission on first composition if not already granted
    LaunchedEffect(initialGranted) {
        if (!initialGranted) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!cameraPermissionGranted) {
        // Permission request in progress
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Izin kamera diperlukan untuk scan barcode",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Izinkan Kamera")
                }
                TextButton(onClick = onClose) {
                    Text("Batalkan", color = Color.Gray)
                }
            }
        }
        return
    }

    CameraBarcodeScannerContent(
        onBarcodeDetected = onBarcodeDetected,
        onClose = onClose
    )
}

@SuppressLint("UnsafeOptInUsageError")
@Composable
private fun CameraBarcodeScannerContent(
    onBarcodeDetected: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var lastScanned by remember { mutableStateOf("") }
    var lastScannedTime by remember { mutableStateOf(0L) }

    val scannerExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose { scannerExecutor.shutdown() }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // CameraX preview
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val mlBarcodeScanner = BarcodeScanning.getClient()

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setTargetResolution(Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(scannerExecutor) { imageProxy ->
                        // @SuppressLint("UnsafeOptInUsageError") on the outer function covers this
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val image = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )
                            mlBarcodeScanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    val barcode = barcodes.firstOrNull {
                                        it.valueType == Barcode.TYPE_PRODUCT ||
                                        it.valueType == Barcode.TYPE_TEXT ||
                                        it.valueType == Barcode.TYPE_ISBN ||
                                        it.valueType == Barcode.TYPE_UNKNOWN
                                    }
                                    val value = barcode?.rawValue
                                    val now = System.currentTimeMillis()
                                    // Debounce: only fire once per second for the same code
                                    if (value != null &&
                                        (value != lastScanned || now - lastScannedTime > 1000L)
                                    ) {
                                        lastScanned = value
                                        lastScannedTime = now
                                        onBarcodeDetected(value)
                                    }
                                }
                                .addOnCompleteListener { imageProxy.close() }
                        } else {
                            imageProxy.close()
                        }
                    }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Scanning frame visual guide
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(260.dp)
                .background(Color.Transparent)
        ) {
            // Corner indicators using small boxes
            val cornerSize = 36.dp
            val cornerThickness = 4.dp
            val cornerColor = Color.White

            // Top-left
            Box(Modifier.align(Alignment.TopStart)) {
                Box(Modifier.width(cornerSize).height(cornerThickness).background(cornerColor))
                Box(Modifier.width(cornerThickness).height(cornerSize).background(cornerColor))
            }
            // Top-right
            Box(Modifier.align(Alignment.TopEnd)) {
                Box(Modifier.width(cornerSize).height(cornerThickness).background(cornerColor).align(Alignment.TopEnd))
                Box(Modifier.width(cornerThickness).height(cornerSize).background(cornerColor).align(Alignment.TopEnd))
            }
            // Bottom-left
            Box(Modifier.align(Alignment.BottomStart)) {
                Box(Modifier.width(cornerSize).height(cornerThickness).background(cornerColor).align(Alignment.BottomStart))
                Box(Modifier.width(cornerThickness).height(cornerSize).background(cornerColor).align(Alignment.BottomStart))
            }
            // Bottom-right
            Box(Modifier.align(Alignment.BottomEnd)) {
                Box(Modifier.width(cornerSize).height(cornerThickness).background(cornerColor).align(Alignment.BottomEnd))
                Box(Modifier.width(cornerThickness).height(cornerSize).background(cornerColor).align(Alignment.BottomEnd))
            }
        }

        // Instructions
        Text(
            text = "Arahkan kamera ke barcode produk",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp)
                .background(Color(0x80000000), RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Close button
        Button(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White.copy(alpha = 0.9f)
            )
        ) {
            Text("Tutup", color = Color.Black)
        }
    }
}
