package id.rancak.app.presentation.auth

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

/**
 * Web Client ID (tipe "Web application") dari Google Cloud Console.
 * BUKAN Android/iOS client ID — harus Web client ID agar backend bisa verifikasi token.
 */
private const val GOOGLE_WEB_CLIENT_ID =
    "222680436513-fhn5h2h047ovbrlssr0v9jflo2i60g99.apps.googleusercontent.com"

@Composable
actual fun GoogleSignInButton(
    modifier: Modifier,
    enabled: Boolean,
    onIdToken: (String) -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    // CredentialManager dibuat sekali per composable lifecycle
    val credentialManager = remember { CredentialManager.create(context) }

    GoogleSignInButtonContent(
        onClick = {
            scope.launch {
                isLoading = true
                try {
                    // GetSignInWithGoogleOption: menampilkan bottom-sheet pemilih
                    // akun Google tanpa memerlukan akun yang sudah ter-authorize.
                    // Ini menggantikan GetGoogleIdOption (One Tap) yang gagal
                    // dengan "No credentials available" pada user pertama kali.
                    val signInOption = GetSignInWithGoogleOption
                        .Builder(GOOGLE_WEB_CLIENT_ID)
                        .build()

                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(signInOption)
                        .build()

                    val result = credentialManager.getCredential(context, request)
                    val credential = result.credential

                    if (credential is CustomCredential &&
                        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                    ) {
                        val googleToken = GoogleIdTokenCredential.createFrom(credential.data)
                        onIdToken(googleToken.idToken)
                    } else {
                        onError("Tipe credential tidak dikenali")
                    }
                } catch (_: GetCredentialCancellationException) {
                    // User menutup dialog — tidak perlu tampilkan error
                } catch (e: GetCredentialException) {
                    onError("Google Sign-In gagal: ${e.message}")
                } catch (e: Exception) {
                    onError("Terjadi kesalahan: ${e.message}")
                } finally {
                    isLoading = false
                }
            }
        },
        modifier = modifier,
        isLoading = isLoading,
        enabled = enabled
    )
}
