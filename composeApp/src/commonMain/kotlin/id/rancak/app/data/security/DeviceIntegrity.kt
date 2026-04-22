package id.rancak.app.data.security

/**
 * Deteksi sederhana apakah device dalam kondisi "tidak aman" (rooted untuk
 * Android, jailbroken untuk iOS). Ini adalah **soft-check** — tidak
 * memblokir penggunaan app, hanya menghasilkan warning yang dapat ditampilkan
 * ke pengguna / dikirim ke telemetry.
 *
 * Heuristik dasar — bukan deteksi tingkat enterprise (untuk itu, gunakan
 * Play Integrity / DeviceCheck di masa depan).
 */
expect object DeviceIntegrity {
    /**
     * True jika device terdeteksi sebagai rooted/jailbroken berdasarkan
     * pemeriksaan ringan: keberadaan file biner root (Android: `su`,
     * `Magisk`; iOS: `/Applications/Cydia.app`, `/private/var/lib/apt`).
     *
     * False positif kecil kemungkinannya karena hanya memeriksa path
     * yang umum. Pemanggil harus memperlakukan hasil true sebagai
     * **indikasi**, bukan vonis pasti.
     */
    fun isCompromised(): Boolean
}
