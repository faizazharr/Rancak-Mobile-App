package id.rancak.app.data.security

import java.io.File

/**
 * Heuristik root detection umum di Android:
 *  - `su` binary di PATH standar
 *  - File Magisk / SuperSU
 *  - Test-keys build (ROM custom sering pakai test-keys)
 *
 * Tidak sepenuhnya tahan bypass — attacker yang menguasai device bisa
 * menyembunyikan jejak ini. Untuk kebutuhan lebih ketat, gunakan
 * Play Integrity API.
 */
actual object DeviceIntegrity {

    private val SU_PATHS = arrayOf(
        "/system/bin/su",
        "/system/xbin/su",
        "/sbin/su",
        "/system/su",
        "/system/bin/.ext/.su",
        "/system/xbin/daemonsu",
        "/data/local/bin/su",
        "/data/local/xbin/su"
    )

    private val ROOT_APP_PATHS = arrayOf(
        "/system/app/Superuser.apk",
        "/system/xbin/busybox",
        "/data/adb/magisk",
        "/sbin/.magisk"
    )

    actual fun isCompromised(): Boolean {
        return hasSuBinary() || hasRootApp() || hasTestKeys()
    }

    private fun hasSuBinary(): Boolean =
        SU_PATHS.any { runCatching { File(it).exists() }.getOrDefault(false) }

    private fun hasRootApp(): Boolean =
        ROOT_APP_PATHS.any { runCatching { File(it).exists() }.getOrDefault(false) }

    private fun hasTestKeys(): Boolean =
        android.os.Build.TAGS?.contains("test-keys") == true
}
