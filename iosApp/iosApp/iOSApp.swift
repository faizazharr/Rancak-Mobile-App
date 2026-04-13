import SwiftUI
import ComposeApp

// AppDelegate diperlukan agar:
//  - BGTaskScheduler.register dipanggil sebelum didFinishLaunchingWithOptions selesai
//  - Koin global diinisialisasi sebelum Compose tree terbentuk
//  - GoogleSignInBridge.launchSignIn di-set sebelum UI pertama kali ditampilkan
//
// Untuk Google Sign-In iOS:
//  1. Tambahkan Google Sign-In SDK via Xcode → File → Add Package Dependencies
//     URL: https://github.com/google/GoogleSignIn-iOS  (versi ~> 8.0)
//  2. Uncomment blok "import GoogleSignIn" dan kode GIDSignIn di bawah
//  3. Isi GIDClientID di Info.plist dengan iOS client ID dari Google Cloud Console
//  4. Tambahkan URL scheme = REVERSED iOS client ID ke Info.plist CFBundleURLTypes
class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        // Inisialisasi Koin + daftarkan BGTask handler
        _ = MainViewControllerKt.MainViewController()

        // ── Google Sign-In Bridge ──────────────────────────────────────────
        // Uncomment setelah menambahkan GoogleSignIn SDK via SPM:
        //
        // import GoogleSignIn   ← tambahkan di atas file ini
        //
        // GoogleSignInBridge.shared.launchSignIn = { completion in
        //     guard let rootVC = UIApplication.shared.connectedScenes
        //         .compactMap({ $0 as? UIWindowScene })
        //         .flatMap({ $0.windows })
        //         .first(where: { $0.isKeyWindow })?.rootViewController
        //     else {
        //         completion.onResult(nil, "Tidak dapat menemukan root view controller")
        //         return
        //     }
        //     GIDSignIn.sharedInstance.signIn(withPresenting: rootVC) { result, error in
        //         if let idToken = result?.user.idToken?.tokenString {
        //             completion.onResult(idToken, nil)
        //         } else {
        //             let msg = error?.localizedDescription ?? "Sign-in dibatalkan"
        //             completion.onResult(nil, msg)
        //         }
        //     }
        // }

        return true
    }

    // Diperlukan agar OAuth redirect Google Sign-In berfungsi
    // Uncomment setelah menambahkan GoogleSignIn SDK:
    //
    // func application(_ app: UIApplication, open url: URL,
    //                  options: [UIApplication.OpenURLOptionsKey: Any] = [:]) -> Bool {
    //     return GIDSignIn.sharedInstance.handle(url)
    // }
}

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
