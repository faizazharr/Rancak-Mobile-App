package id.rancak.app.presentation.barcode

import androidx.compose.runtime.Composable

/**
 * Platform-specific barcode scanner composable.
 *
 * On Android: opens a CameraX preview with ML Kit barcode detection.
 * On iOS: uses AVFoundation (placeholder — integrate with native iOS scanner).
 *
 * @param onBarcodeDetected Called with the raw barcode string when a barcode is scanned.
 * @param onClose Called when the user dismisses the scanner.
 */
@Composable
expect fun BarcodeScannerView(
    onBarcodeDetected: (String) -> Unit,
    onClose: () -> Unit
)
