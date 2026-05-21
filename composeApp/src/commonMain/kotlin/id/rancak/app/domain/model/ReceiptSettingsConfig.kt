package id.rancak.app.domain.model

/**
 * Konfigurasi tampilan struk per tenant.
 *
 * Semua field nullable di-skip saat rendering (tidak dicetak).
 * Field NOT NULL memiliki nilai default yang disediakan server.
 */
data class ReceiptSettingsConfig(
    /** URL logo toko (di-upload ke Cloudflare). Null = tidak ada logo. */
    val logoUrl: String? = null,
    /** Email toko — dicetak jika diisi. */
    val email: String? = null,
    /** Website toko — dicetak jika diisi. */
    val website: String? = null,
    /** NPWP toko — dicetak jika diisi. */
    val npwp: String? = null,
    /** Teks bebas header di bawah identitas toko. */
    val receiptHeader: String? = null,
    /** Baris pertama footer (ucapan terima kasih, dll.). */
    val receiptFooter: String? = null,
    /** Baris kedua footer (kebijakan retur, promo, dll.). */
    val receiptFooter2: String? = null,
    /** Posisi logo: "left" | "center" | "right" */
    val logoPosition: String = "center",
    /** Lebar logo sebagai % dari lebar kertas: 60 | 70 | 80 | 100 */
    val logoSizePct: Int = 80,
    /** Ukuran font nama toko: "normal" | "large" | "xlarge" */
    val receiptNameSize: String = "large",
    /** Gaya garis pemisah: "dashed" | "double" | "none" */
    val separatorStyle: String = "dashed",
    /** Jumlah baris pemisah: 1 | 2 */
    val separatorCount: Int = 1,
    /** Alignment footer: "left" | "center" | "right" */
    val footerPosition: String = "center",
    /** Username Instagram (tanpa @). Dicetak sebagai "IG: @username". */
    val receiptInstagram: String? = null,
    /** Username/URL Facebook. Dicetak sebagai "FB: nilai". */
    val receiptFacebook: String? = null,
    /** Nama SSID WiFi toko. */
    val receiptWifiSsid: String? = null,
    /** Password WiFi — hanya tampil jika wifiSsid juga diisi. */
    val receiptWifiPassword: String? = null
)
