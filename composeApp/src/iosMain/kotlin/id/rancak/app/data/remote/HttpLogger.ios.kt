package id.rancak.app.data.remote

import io.ktor.client.plugins.logging.Logger

actual fun platformHttpLogger(): Logger = object : Logger {
    override fun log(message: String) {
        println("[Ktor] $message")
    }
}
