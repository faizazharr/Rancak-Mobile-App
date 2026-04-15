package id.rancak.app.presentation.components

import androidx.compose.runtime.Composable

/**
 * Returns a lambda that requests Bluetooth permission at runtime.
 *
 * - Android (API 31+): requests BLUETOOTH_CONNECT via ActivityResultContracts.
 * - Android (<31): immediately calls onResult(true).
 * - iOS: immediately calls onResult(true) — BLE permissions handled by Info.plist.
 */
@Composable
expect fun rememberRequestBluetoothPermission(onResult: (Boolean) -> Unit): () -> Unit
