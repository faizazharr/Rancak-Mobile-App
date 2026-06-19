package id.rancak.app.data.security

import java.io.File

/**
 * Heuristik root detection di Android. Mencakup su binaries, Magisk,
 * SuperSU, Xposed, Frida, test-keys, dan write access ke /system.
 *
 * Tidak sepenuhnya tahan bypass — attacker yang menguasai device bisa
 * menyembunyikan jejak ini. Untuk kebutuhan lebih ketat, gunakan
 * Play Integrity API.
 */
actual object DeviceIntegrity {
    private val SU_PATHS =
        arrayOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/system/su",
            "/system/bin/.ext/.su",
            "/system/xbin/daemonsu",
            "/data/local/bin/su",
            "/data/local/xbin/su",
            "/data/local/su",
        )

    private val SUSPICIOUS_PATHS =
        arrayOf(
            "/system/app/Superuser.apk",
            "/system/xbin/busybox",
            "/data/adb/magisk",
            "/sbin/.magisk",
            "/data/adb/modules",
            "/system/lib/libxposed_art.so",
            "/system/framework/XposedBridge.jar",
            "/data/data/de.robv.android.xposed.installer",
            "/data/data/io.va.exposed",
            "/data/local/tmp/frida-server",
            "/data/local/tmp/re.frida.server",
        )

    actual fun isCompromised(): Boolean {
        return hasSuBinary() ||
            hasSuspiciousPath() ||
            hasTestKeys() ||
            canWriteToSystem() ||
            isSuExecutable()
    }

    private fun hasSuBinary(): Boolean = SU_PATHS.any { runCatching { File(it).exists() }.getOrDefault(false) }

    private fun hasSuspiciousPath(): Boolean = SUSPICIOUS_PATHS.any { runCatching { File(it).exists() }.getOrDefault(false) }

    private fun hasTestKeys(): Boolean = android.os.Build.TAGS?.contains("test-keys") == true

    private fun canWriteToSystem(): Boolean =
        runCatching {
            val f = File("/system/rancak_probe")
            f.createNewFile().also { if (it) f.delete() }
        }.getOrDefault(false)

    private fun isSuExecutable(): Boolean =
        runCatching {
            Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
                .waitFor() == 0
        }.getOrDefault(false)
}
