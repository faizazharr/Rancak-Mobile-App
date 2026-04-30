package id.rancak.app.data.printing

/**
 * Renders [ReceiptData] sebagai plain-text monospaced — mirror dari layout
 * ESC/POS di [EscPosBuilder.buildReceipt] tapi tanpa byte command.
 *
 * Dipakai untuk **preview struk** di halaman Pengaturan supaya user bisa lihat
 * persis seperti apa hasil cetak nanti, tanpa harus benar-benar ngeprint.
 *
 * Lebar kolom mengikuti paper size:
 * - 58 mm  → 32 karakter
 * - 80 mm  → 48 karakter
 */
object ReceiptTextRenderer {

    fun render(data: ReceiptData, paperWidthMm: Int = 58): String {
        val width = lineWidthFor(paperWidthMm)
        val sb = StringBuilder()

        // ── Header ──────────────────────────────────────────────────────────
        sb.appendCenter(data.storeName.uppercase(), width)
        if (!data.storeAddress.isNullOrBlank()) sb.appendCenter(data.storeAddress, width)
        if (!data.storePhone.isNullOrBlank())   sb.appendCenter(data.storePhone, width)
        sb.appendLine()

        if (data.isVoided) {
            sb.appendCenter("*** VOID ***", width)
            sb.appendLine()
        }

        // ── Invoice info ────────────────────────────────────────────────────
        sb.appendDivider('=', width)
        sb.appendPadded("#${data.invoiceNo}", data.orderType.uppercase(), width)
        if (!data.tableName.isNullOrBlank())   sb.appendLine("Meja  : ${data.tableName}")
        if (!data.cashierName.isNullOrBlank()) sb.appendLine("Kasir : ${data.cashierName}")
        if (data.createdAt.isNotBlank())       sb.appendLine(data.createdAt)
        sb.appendDivider('-', width)

        // ── Items ───────────────────────────────────────────────────────────
        for (item in data.items) {
            val nameDisplay = if (!item.variantName.isNullOrBlank())
                "${item.name} (${item.variantName})"
            else item.name
            val maxName = width - 10
            val truncated = if (nameDisplay.length > maxName)
                nameDisplay.substring(0, (maxName - 1).coerceAtLeast(1)) + "…"
            else nameDisplay
            sb.appendLine(truncated)

            val qtyPrice = "  ${item.qty}x ${rupiah(item.price)}"
            sb.appendPadded(qtyPrice, rupiah(item.subtotal), width)
            if (!item.note.isNullOrBlank()) sb.appendLine("  *${item.note}")
        }

        sb.appendDivider('-', width)

        // ── Totals ──────────────────────────────────────────────────────────
        sb.appendPadded("Subtotal", rupiah(data.subtotal), width)
        if (data.discount    > 0) sb.appendPadded("Diskon",    "-${rupiah(data.discount)}", width)
        if (data.surcharge   > 0) sb.appendPadded("Surcharge", rupiah(data.surcharge), width)
        if (data.tax         > 0) sb.appendPadded("Pajak",     rupiah(data.tax), width)
        if (data.deliveryFee > 0) sb.appendPadded("Ongkir",    rupiah(data.deliveryFee), width)
        if (data.tip         > 0) sb.appendPadded("Tip",       rupiah(data.tip), width)

        sb.appendDivider('=', width)
        sb.appendPadded("TOTAL", rupiah(data.total), width)

        if (!data.paymentMethod.isNullOrBlank()) {
            sb.appendPadded("Bayar (${data.paymentMethod.uppercase()})", rupiah(data.paidAmount), width)
            if (data.changeAmount > 0) sb.appendPadded("Kembalian", rupiah(data.changeAmount), width)
        }

        sb.appendDivider('=', width)

        // ── Footer ──────────────────────────────────────────────────────────
        sb.appendLine()
        val footer = data.footerText ?: "Terima Kasih!"
        sb.appendCenter(footer, width)
        if (data.footerText == null) sb.appendCenter("Sampai jumpa kembali :)", width)

        return sb.toString().trimEnd('\n')
    }

    /** Sample receipt untuk dipakai sebagai preview ketika user belum punya transaksi. */
    fun sample(
        storeName: String,
        storeAddress: String?,
        storePhone: String?,
        footerText: String?,
        cashierName: String? = "Kasir 1"
    ): ReceiptData = ReceiptData(
        storeName     = storeName.ifBlank { "Rancak POS" },
        storeAddress  = storeAddress?.ifBlank { null },
        storePhone    = storePhone?.ifBlank { null },
        invoiceNo     = "INV-001",
        orderType     = "dine_in",
        tableName     = "A1",
        cashierName   = cashierName,
        createdAt     = "01/05/2026 18:30",
        items         = listOf(
            ReceiptItem(name = "Nasi Goreng Spesial", qty = 2, price = 25_000, subtotal = 50_000),
            ReceiptItem(name = "Es Teh Manis",        qty = 2, price = 5_000,  subtotal = 10_000, note = "Tidak terlalu manis"),
            ReceiptItem(name = "Ayam Bakar",          variantName = "Pedas", qty = 1, price = 30_000, subtotal = 30_000)
        ),
        subtotal      = 90_000,
        discount      = 5_000,
        surcharge     = 0,
        tax           = 8_500,
        total         = 93_500,
        paymentMethod = "cash",
        paidAmount    = 100_000,
        changeAmount  = 6_500,
        footerText    = footerText?.ifBlank { null }
    )

    // ── Internal helpers ────────────────────────────────────────────────────

    private fun lineWidthFor(paperWidthMm: Int): Int = when (paperWidthMm) {
        80   -> 48
        58   -> 32
        else -> 32
    }

    private fun StringBuilder.appendCenter(s: String, width: Int) {
        val pad = ((width - s.length) / 2).coerceAtLeast(0)
        appendLine(" ".repeat(pad) + s)
    }

    private fun StringBuilder.appendDivider(ch: Char, width: Int) {
        appendLine(ch.toString().repeat(width))
    }

    private fun StringBuilder.appendPadded(left: String, right: String, width: Int) {
        val spaces = width - left.length - right.length
        appendLine(if (spaces <= 0) "$left $right" else left + " ".repeat(spaces) + right)
    }

    private fun rupiah(amount: Long): String {
        if (amount == 0L) return "0"
        return buildString {
            val str = amount.toString()
            var count = 0
            for (i in str.indices.reversed()) {
                if (count > 0 && count % 3 == 0) insert(0, '.')
                insert(0, str[i])
                count++
            }
        }
    }
}
