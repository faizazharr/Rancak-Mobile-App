package id.rancak.app.presentation.barcode

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.time.Clock
import platform.AVFoundation.*
import platform.CoreGraphics.*
import platform.Foundation.NSError
import platform.UIKit.*
import platform.darwin.NSObject
import platform.darwin.dispatch_get_main_queue
import kotlin.coroutines.resume

/**
 * iOS barcode scanner menggunakan AVFoundation.
 *
 * Mendukung format:
 *   - QR Code
 *   - EAN-13 / EAN-8 (produk retail)
 *   - Code 128 (warehouse / logistik)
 *   - Code 39
 *   - Data Matrix
 *
 * SETUP WAJIB DI XCODE:
 *   Tambahkan ke Info.plist:
 *     <key>NSCameraUsageDescription</key>
 *     <string>Dibutuhkan untuk scan barcode produk</string>
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun BarcodeScannerView(
    onBarcodeDetected: (String) -> Unit,
    onClose: () -> Unit
) {
    // null = checking, true = granted, false = denied
    var hasPermission by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
        hasPermission = when (status) {
            AVAuthorizationStatusAuthorized -> true
            AVAuthorizationStatusNotDetermined -> {
                // Minta izin — callback dipanggil di main thread
                suspendCancellableCoroutine { cont ->
                    AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                        cont.resume(granted)
                    }
                }
            }
            else -> false  // AVAuthorizationStatusDenied / Restricted
        }
    }

    when (hasPermission) {
        true  -> CameraBarcodeScannerContent(
            onBarcodeDetected = onBarcodeDetected,
            onClose           = onClose
        )
        false -> NoCameraPermissionContent(onClose = onClose)
        null  -> Box(
            modifier          = Modifier.fillMaxSize().background(Color.Black),
            contentAlignment  = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Camera preview + barcode detection
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalForeignApi::class)
@Composable
private fun CameraBarcodeScannerContent(
    onBarcodeDetected: (String) -> Unit,
    onClose: () -> Unit
) {
    // Delegate hidup selama composable aktif
    val scannerDelegate = remember { BarcodeScannerDelegate(onBarcodeDetected) }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Native camera preview (UIKitView) ──────────────────────────────────
        UIKitView(
            factory = {
                val containerView = UIView()
                containerView.backgroundColor = UIColor.blackColor

                val session = AVCaptureSession()
                session.sessionPreset = AVCaptureSessionPreset1280x720

                // Input: kamera belakang
                val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
                if (device != null) {
                    @Suppress("UNCHECKED_CAST")
                    val input = AVCaptureDeviceInput.deviceInputWithDevice(
                        device, null as CPointer<ObjCObjectVar<NSError?>>?
                    ) as? AVCaptureDeviceInput
                    if (input != null && session.canAddInput(input)) {
                        session.addInput(input)
                    }
                }

                // Output: metadata (barcode detection)
                val metadataOutput = AVCaptureMetadataOutput()
                if (session.canAddOutput(metadataOutput)) {
                    session.addOutput(metadataOutput)
                    // Delegate harus diset SETELAH addOutput agar metadataObjectTypes valid
                    metadataOutput.setMetadataObjectsDelegate(
                        scannerDelegate,
                        queue = dispatch_get_main_queue()
                    )
                    metadataOutput.metadataObjectTypes = listOf(
                        AVMetadataObjectTypeQRCode,
                        AVMetadataObjectTypeEAN13Code,
                        AVMetadataObjectTypeEAN8Code,
                        AVMetadataObjectTypeCode128Code,
                        AVMetadataObjectTypeCode39Code,
                        AVMetadataObjectTypeDataMatrixCode
                    )
                }

                // Preview layer
                val previewLayer = AVCaptureVideoPreviewLayer(session = session)
                previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
                containerView.layer.addSublayer(previewLayer)

                // Simpan referensi untuk resize & cleanup
                scannerDelegate.session      = session
                scannerDelegate.previewLayer = previewLayer

                session.startRunning()
                containerView
            },
            onResize = { _, rect ->
                // Update frame preview layer setiap kali ukuran view berubah
                scannerDelegate.previewLayer?.setFrame(rect)
            },
            onRelease = { _ ->
                // Stop capture session saat composable dihapus dari tree
                scannerDelegate.session?.stopRunning()
            },
            modifier = Modifier.fillMaxSize()
        )

        // ── Close button ──────────────────────────────────────────────────────
        Box(
            modifier         = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(end = 8.dp, top = 4.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            IconButton(onClick = {
                scannerDelegate.session?.stopRunning()
                onClose()
            }) {
                Icon(
                    imageVector        = Icons.Default.Close,
                    contentDescription = "Tutup scanner",
                    tint               = Color.White
                )
            }
        }

        // ── Viewfinder overlay ────────────────────────────────────────────────
        Column(
            modifier              = Modifier.fillMaxSize(),
            horizontalAlignment   = Alignment.CenterHorizontally,
            verticalArrangement   = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Frame sudut (corner bracket indicator)
            val frameSize = 240.dp
            val cornerColor = Color.White
            val cornerLength = 32.dp
            val strokeWidth = 3.dp

            Canvas(modifier = Modifier.size(frameSize)) {
                val sw = strokeWidth.toPx()
                val cl = cornerLength.toPx()
                val w = size.width
                val h = size.height

                // Top-left
                drawLine(cornerColor, Offset(0f, 0f), Offset(cl, 0f), sw)
                drawLine(cornerColor, Offset(0f, 0f), Offset(0f, cl), sw)
                // Top-right
                drawLine(cornerColor, Offset(w, 0f), Offset(w - cl, 0f), sw)
                drawLine(cornerColor, Offset(w, 0f), Offset(w, cl), sw)
                // Bottom-left
                drawLine(cornerColor, Offset(0f, h), Offset(cl, h), sw)
                drawLine(cornerColor, Offset(0f, h), Offset(0f, h - cl), sw)
                // Bottom-right
                drawLine(cornerColor, Offset(w, h), Offset(w - cl, h), sw)
                drawLine(cornerColor, Offset(w, h), Offset(w, h - cl), sw)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text      = "Arahkan kamera ke barcode produk",
                color     = Color.White.copy(alpha = 0.85f),
                style     = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1.5f))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// No permission UI
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NoCameraPermissionContent(onClose: () -> Unit) {
    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier              = Modifier.padding(32.dp),
            horizontalAlignment   = Alignment.CenterHorizontally,
            verticalArrangement   = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text  = "Akses Kamera Diperlukan",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text      = "Buka Settings › Privacy & Security › Kamera,\n" +
                             "lalu aktifkan akses untuk aplikasi ini.",
                color     = Color.White.copy(alpha = 0.7f),
                style     = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onClose,
                colors  = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("Tutup")
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AVCaptureMetadataOutput delegate
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalForeignApi::class)
private class BarcodeScannerDelegate(
    private val onDetected: (String) -> Unit
) : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {

    var session:      AVCaptureSession?          = null
    var previewLayer: AVCaptureVideoPreviewLayer? = null

    /** Epoch millis saat barcode terakhir berhasil di-scan (debounce 1 detik). */
    private var lastScanTime = 0L

    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputMetadataObjects: List<*>,
        fromConnection: AVCaptureConnection
    ) {
        val now = Clock.System.now().toEpochMilliseconds()
        if (now - lastScanTime < 1_000L) return  // debounce 1 detik

        val barcode = didOutputMetadataObjects
            .filterIsInstance<AVMetadataMachineReadableCodeObject>()
            .firstOrNull()
            ?.stringValue
            ?: return

        lastScanTime = now
        onDetected(barcode)
    }
}
