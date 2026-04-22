package id.rancak.app.presentation.auth

import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialInterruptedException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.GetCredentialUnsupportedException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import id.rancak.app.BuildConfig
import kotlinx.coroutines.launch

private const val TAG = "GoogleSignIn"

/** Log debug-only — tidak akan muncul di release build. */
private inline fun dlog(message: () -> String) {
    if (BuildConfig.DEBUG) Log.d(TAG, message())
}

/**
 * Web Client ID (tipe "Web application") dari Google Cloud Console.
 * BUKAN Android/iOS client ID — harus Web client ID agar backend bisa verifikasi token.
 */
private const val GOOGLE_WEB_CLIENT_ID =
    "222680436513-jmpqs7vrht86n168nrmhemg3neenvqdu.apps.googleusercontent.com"

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
                dlog { "Tombol Google Sign-In ditekan" }
                try {
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
                        dlog { "ID Token diterima (length=${googleToken.idToken.length})" }
                        onIdToken(googleToken.idToken)
                    } else {
                        Log.w(TAG, "Credential type tidak dikenali")
                        onError("Tipe credential tidak dikenali")
                    }
                } catch (e: GetCredentialCancellationException) {
                    dlog { "User membatalkan Google Sign-In" }
                    // Dibatalkan user — tidak perlu tampilkan error
                } catch (e: NoCredentialException) {
                    Log.w(TAG, "NoCredentialException")
                    onError("Tidak ada akun Google yang tersedia. Tambahkan akun Google di Pengaturan perangkat.")
                } catch (e: GetCredentialUnsupportedException) {
                    Log.w(TAG, "GetCredentialUnsupportedException")
                    onError("Google Sign-In tidak didukung di perangkat ini. Pastikan Google Play Services ter-install.")
                } catch (e: GetCredentialProviderConfigurationException) {
                    Log.w(TAG, "GetCredentialProviderConfigurationException")
                    onError("Konfigurasi Google Sign-In belum lengkap. Hubungi tim pengembang.")
                } catch (e: GetCredentialInterruptedException) {
                    Log.w(TAG, "GetCredentialInterruptedException")
                    onError("Google Sign-In dibatalkan. Silakan coba lagi.")
                } catch (e: GetCredentialException) {
                    Log.w(TAG, "GetCredentialException type=${e.type}")
                    onError("Google Sign-In gagal. Pastikan koneksi internet tersambung dan coba lagi.")
                } catch (e: Exception) {
                    Log.e(TAG, "Exception tidak terduga [${e::class.simpleName}]")
                    onError("Terjadi kesalahan saat login. Silakan coba lagi.")
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
