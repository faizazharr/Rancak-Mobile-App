package id.rancak.app.presentation.components

import androidx.compose.runtime.Composable

@Composable
actual fun rememberRequestBluetoothPermission(onResult: (Boolean) -> Unit): () -> Unit {
    // iOS: BLE permissions are handled declaratively via Info.plist
    // (NSBluetoothAlwaysUsageDescription). No runtime request needed.
    return { onResult(true) }
}
