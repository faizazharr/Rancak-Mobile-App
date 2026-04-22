package id.rancak.app.data.remote

import io.ktor.client.plugins.logging.Logger

/**
 * Platform-specific Ktor [Logger] so HTTP logs appear in the correct output sink:
 * - Android → android.util.Log.d (visible in Logcat with tag "Ktor")
 * - iOS     → NSLog / print (visible in Xcode console)
 */
expect fun platformHttpLogger(): Logger

/**
 * True jika app dibuild sebagai DEBUG. Dipakai untuk menon-aktifkan logging
 * HTTP verbose di release build (menghindari kebocoran PII/token).
 */
expect fun isDebugBuild(): Boolean
