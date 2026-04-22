package id.rancak.app.data.security

import platform.Foundation.NSFileManager

/**
 * Heuristik jailbreak detection umum di iOS — cek keberadaan file/path
 * yang hanya ada pada device jailbroken (Cydia, Sileo, apt, Substrate).
 *
 * Tidak sepenuhnya tahan bypass — tweak seperti FlyJB / Shadow dapat
 * menyembunyikan jejak ini. Untuk kebutuhan lebih ketat, gunakan
 * DeviceCheck / App Attest.
 */
actual object DeviceIntegrity {

    private val JAILBREAK_PATHS = arrayOf(
        "/Applications/Cydia.app",
        "/Applications/Sileo.app",
        "/Applications/Zebra.app",
        "/Library/MobileSubstrate/MobileSubstrate.dylib",
        "/bin/bash",
        "/usr/sbin/sshd",
        "/etc/apt",
        "/private/var/lib/apt",
        "/private/var/lib/cydia",
        "/private/var/stash"
    )

    actual fun isCompromised(): Boolean {
        val fm = NSFileManager.defaultManager
        return JAILBREAK_PATHS.any { fm.fileExistsAtPath(it) }
    }
}
