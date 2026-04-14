package id.rancak.app.presentation.auth

/**
 * Callback interface yang dipanggil Swift setelah proses sign-in selesai.
 * Accessible dari Swift sebagai:
 *   completion.onResult("id_token", nil)   // berhasil
 *   completion.onResult(nil, "error msg")  // gagal
 */
fun interface GoogleSignInCompletion {
    fun onResult(idToken: String?, error: String?)
}

/**
 * Bridge antara Kotlin KMM dan Google Sign-In iOS SDK (Swift).
 *
 * Setup di iOSApp.swift (di dalam AppDelegate.application(_:didFinishLaunchingWithOptions:)):
 *
 * ```swift
 * import GoogleSignIn  // tambahkan via SPM di Xcode
 *
 * GoogleSignInBridge.shared.launchSignIn = { completion in
 *     guard let rootVC = UIApplication.shared.connectedScenes
 *         .compactMap({ $0 as? UIWindowScene })
 *         .flatMap({ $0.windows })
 *         .first(where: { $0.isKeyWindow })?.rootViewController
 *     else {
 *         completion.onResult(nil, "Tidak dapat menemukan root view controller")
 *         return
 *     }
 *     GIDSignIn.sharedInstance.signIn(withPresenting: rootVC) { result, error in
 *         if let idToken = result?.user.idToken?.tokenString {
 *             completion.onResult(idToken, nil)
 *         } else {
 *             completion.onResult(nil, error?.localizedDescription ?? "Sign-in dibatalkan")
 *         }
 *     }
 * }
 * ```
 *
 * Prasyarat iOS:
 *  1. Tambahkan Google Sign-In iOS SDK via Xcode → File → Add Package Dependencies
 *     URL: https://github.com/google/GoogleSignIn-iOS
 *  2. Tambahkan ke Info.plist:
 *       GIDClientID  → iOS client ID dari Google Cloud Console
 *       CFBundleURLTypes → URL scheme = REVERSED iOS client ID
 *  3. Di AppDelegate, tambahkan handle(url:) untuk redirect OAuth
 */
object GoogleSignInBridge {
    /**
     * Swift mengisi property ini saat app launch.
     * Kotlin memanggil lambda ini lalu suspend menunggu [GoogleSignInCompletion.onResult].
     */
    var launchSignIn: ((GoogleSignInCompletion) -> Unit)? = null
}
