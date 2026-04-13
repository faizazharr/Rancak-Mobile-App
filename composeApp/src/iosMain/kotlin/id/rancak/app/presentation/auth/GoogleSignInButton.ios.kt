package id.rancak.app.presentation.auth

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Composable
actual fun GoogleSignInButton(
    modifier: Modifier,
    enabled: Boolean,
    onIdToken: (String) -> Unit,
    onError: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    GoogleSignInButtonContent(
        onClick = {
            scope.launch {
                val launcher = GoogleSignInBridge.launchSignIn
                if (launcher == null) {
                    onError(
                        "Google Sign-In belum dikonfigurasi. " +
                        "Lihat GoogleSignInBridge.kt untuk panduan setup iOS."
                    )
                    return@launch
                }

                isLoading = true
                val (idToken, error) = suspendCancellableCoroutine { cont ->
                    launcher { token, err ->
                        if (cont.isActive) cont.resume(Pair(token, err))
                    }
                }
                isLoading = false

                when {
                    idToken != null -> onIdToken(idToken)
                    error  != null  -> onError(error)
                    // else: dibatalkan user, diam saja
                }
            }
        },
        modifier = modifier,
        isLoading = isLoading,
        enabled = enabled
    )
}
