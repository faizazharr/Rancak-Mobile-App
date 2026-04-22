package id.rancak.app.data.remote

import android.util.Log
import io.ktor.client.plugins.logging.Logger

actual fun platformHttpLogger(): Logger = object : Logger {
    override fun log(message: String) {
        // Split on newlines so each line appears cleanly in Logcat (max 4000 chars/line)
        message.chunked(3900).forEach { chunk ->
            Log.d("Ktor", chunk)
        }
    }
}
