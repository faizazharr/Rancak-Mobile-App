package id.rancak.app.data.remote

import io.ktor.client.plugins.logging.Logger
import platform.Foundation.NSProcessInfo

actual fun platformHttpLogger(): Logger = object : Logger {
    override fun log(message: String) {
        println("[Ktor] $message")
    }
}

/**
 * iOS: pakai environment variable `DEBUG` yang di-set Xcode untuk Debug build.
 * Alternatif: Konfigurasikan BuildKonfig / expect actual dari Gradle.
 */
actual fun isDebugBuild(): Boolean {
    return NSProcessInfo.processInfo.environment["DEBUG"] != null
}
