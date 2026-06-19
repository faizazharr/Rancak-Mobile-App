package id.rancak.app.data.security

import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile

/**
 * Heuristik jailbreak detection di iOS — cek file, symlink, dan write access
 * yang hanya ada pada device jailbroken (Cydia, Sileo, apt, Substrate, Frida).
 *
 * Tidak sepenuhnya tahan bypass — tweak seperti FlyJB / Shadow dapat
 * menyembunyikan jejak ini. Untuk kebutuhan lebih ketat, gunakan
 * DeviceCheck / App Attest.
 */
@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual object DeviceIntegrity {
    private val JAILBREAK_PATHS =
        arrayOf(
            "/Applications/Cydia.app",
            "/Applications/Sileo.app",
            "/Applications/Zebra.app",
            "/Applications/Filza.app",
            "/Library/MobileSubstrate/MobileSubstrate.dylib",
            "/Library/MobileSubstrate/DynamicLibraries/Veency.plist",
            "/Library/MobileSubstrate/DynamicLibraries/LiveClock.plist",
            "/bin/bash",
            "/bin/sh",
            "/usr/sbin/sshd",
            "/usr/bin/ssh",
            "/usr/libexec/sftp-server",
            "/etc/apt",
            "/etc/ssh/sshd_config",
            "/private/var/lib/apt",
            "/private/var/lib/cydia",
            "/private/var/stash",
            "/private/var/mobile/Library/SBSettings/Themes",
            "/var/checkra1n.dmg",
            "/var/binpack",
            "/usr/local/bin/frida-server",
            "/data/FridaGadget.dylib",
        )

    actual fun isCompromised(): Boolean {
        return hasJailbreakPath() || canWriteOutsideSandbox()
    }

    private fun hasJailbreakPath(): Boolean {
        val fm = NSFileManager.defaultManager
        return JAILBREAK_PATHS.any { fm.fileExistsAtPath(it) }
    }

    private fun canWriteOutsideSandbox(): Boolean =
        runCatching {
            val probe = "/private/rancak_probe.txt"
            val result = NSString.stringWithContentsOfFile(probe, NSUTF8StringEncoding, null)
            result != null
        }.getOrDefault(false)
}
