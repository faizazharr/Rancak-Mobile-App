import SwiftUI
import ComposeApp

// AppDelegate diperlukan agar BGTaskScheduler.register bisa dipanggil
// sebelum application(_:didFinishLaunchingWithOptions:) selesai.
// MainViewControllerKt.MainViewController() juga dipanggil dari sini untuk
// inisialisasi Koin global sebelum Compose tree terbentuk.
class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        // Inisialisasi Koin + daftarkan BGTask handler
        // (MainViewController() mengecek flag agar tidak dobel inisialisasi)
        _ = MainViewControllerKt.MainViewController()
        return true
    }
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