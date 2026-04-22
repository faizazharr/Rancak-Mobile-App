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
import kotlinx.coroutines.launch

private const val TAG = "GoogleSignIn"

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
                Log.d(TAG, "Tombol Google Sign-In ditekan")
                try {
                    Log.d(TAG, "Context type: ${context::class.simpleName}")
                    Log.d(TAG, "Membuat GetSignInWithGoogleOption dengan Web Client ID: $GOOGLE_WEB_CLIENT_ID")

                    val signInOption = GetSignInWithGoogleOption
                        .Builder(GOOGLE_WEB_CLIENT_ID)
                        .build()

                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(signInOption)
                        .build()

                    Log.d(TAG, "Memanggil credentialManager.getCredential(...)")
                    val result = credentialManager.getCredential(context, request)
                    val credential = result.credential

                    Log.d(TAG, "Credential diterima: type=${credential.type}")

                    if (credential is CustomCredential &&
                        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                    ) {
                        val googleToken = GoogleIdTokenCredential.createFrom(credential.data)
                        Log.d(TAG, "ID Token berhasil didapat (length=${googleToken.idToken.length})")
                        onIdToken(googleToken.idToken)
                    } else {
                        Log.w(TAG, "Credential type tidak dikenali: ${credential.type}")
                        onError("Tipe credential tidak dikenali")
                    }
                } catch (e: GetCredentialCancellationException) {
                    Log.d(TAG, "User membatalkan Google Sign-In")
                    // Dibatalkan user — tidak perlu tampilkan error
                } catch (e: NoCredentialException) {
                    Log.e(TAG, "NoCredentialException: ${e.message}", e)
                    onError("Tidak ada akun Google yang tersedia. Tambahkan akun Google di Pengaturan perangkat.")
                } catch (e: GetCredentialUnsupportedException) {
                    Log.e(TAG, "GetCredentialUnsupportedException: ${e.message}", e)
                    onError("Google Sign-In tidak didukung di perangkat ini. Pastikan Google Play Services ter-install.")
                } catch (e: GetCredentialProviderConfigurationException) {
                    Log.e(TAG, "GetCredentialProviderConfigurationException: ${e.message}", e)
                    onError("Konfigurasi Google Sign-In belum lengkap. Hubungi tim pengembang.")
                } catch (e: GetCredentialInterruptedException) {
                    Log.e(TAG, "GetCredentialInterruptedException: ${e.message}", e)
                    onError("Google Sign-In dibatalkan. Silakan coba lagi.")
                } catch (e: GetCredentialException) {
                    Log.e(TAG, "GetCredentialException [${e::class.simpleName}]: type=${e.type} msg=${e.message}", e)
                    onError("Google Sign-In gagal. Pastikan koneksi internet tersambung dan coba lagi.")
                } catch (e: Exception) {
                    Log.e(TAG, "Exception tidak terduga [${e::class.simpleName}]: ${e.message}", e)
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
