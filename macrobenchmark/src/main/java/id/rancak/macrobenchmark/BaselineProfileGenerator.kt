package id.rancak.macrobenchmark

import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates the Baseline Profile yang di-embed ke APK release oleh AGP.
 *
 * Cara generate:
 *   1. Sambungkan perangkat fisik API 28+ (atau emulator dengan API 28+)
 *   2. Jalankan: ./gradlew :composeApp:generateReleaseBaselineProfile
 *   3. Profile akan tersimpan di composeApp/src/main/generated/baselineProfiles/
 *
 * Profile di-apply otomatis saat install via `profileinstaller` (debug) atau
 * lewat Play Store (release), sehingga cold-start Compose lebih cepat.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        rule.collect(
            packageName = "id.rancak.app",
            includeInStartupProfile = true,
            stableIterations = 3,
        ) {
            // Cold-start dari home screen
            pressHome()
            startActivityAndWait()

            // Tunggu splash/splash selesai dan layar utama muncul
            device.waitForIdle(3_000)

            // Tunggu sampai ada content yang render (login / POS screen)
            device.wait(Until.hasObject(By.depth(3)), 5_000)
        }
    }
}

/**
 * Startup benchmark — mengukur actual cold-start time setelah profile di-apply.
 * Jalankan via: ./gradlew :macrobenchmark:connectedBenchmarkAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {
    @get:Rule
    val benchmarkRule = androidx.benchmark.macro.junit4.MacrobenchmarkRule()

    @Test
    fun startup() =
        benchmarkRule.measureRepeated(
            packageName = "id.rancak.app",
            metrics = listOf(androidx.benchmark.macro.StartupTimingMetric()),
            iterations = 5,
            startupMode = StartupMode.COLD,
        ) {
            pressHome()
            startActivityAndWait()
        }
}
