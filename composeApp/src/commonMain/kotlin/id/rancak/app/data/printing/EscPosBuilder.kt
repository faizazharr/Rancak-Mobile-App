package id.rancak.app.data.printing

/**
 * Pure Kotlin ESC/POS receipt byte builder.
 * No platform dependencies — runs identically on Android and iOS.
 *
 * ESC/POS command reference used here:
 *   ESC @ (1B 40)       — Initialize / reset printer
 *   ESC a n (1B 61 n)   — Justify: 0=left, 1=center, 2=right
 *   ESC E n (1B 45 n)   — Bold: 1=on, 0=off
 *   ESC ! n (1B 21 n)   — Character size: 0=normal, 16=double-height, 32=double-width, 48=double
 *   LF (0A)             — Line feed (advance paper one line)
 *   GS V n (1D 56 n)    — Cut paper: 0=full cut, 1=partial cut
 */
object EscPosBuilder {

    private const val LINE_WIDTH = 32  // Characters per line for 58mm paper

    fun buildReceipt(data: ReceiptData): ByteArray {
        val buf = mutableListOf<Byte>()

        // ── ESC/POS helper lambdas ──────────────────────────────────────────
        fun raw(vararg bytes: Int)  = bytes.forEach { buf.add(it.toByte()) }
        fun text(s: String)         = s.toByteArray(Charsets.UTF_8).forEach { buf.add(it) }
        fun lf()                    = buf.add(0x0A.toByte())
        fun init()                  = raw(0x1B, 0x40)
        fun center()                = raw(0x1B, 0x61, 0x01)
        fun left()                  = raw(0x1B, 0x61, 0x00)
        fun bold(on: Boolean)       = raw(0x1B, 0x45, if (on) 0x01 else 0x00)
        fun doubleHeight(on: Boolean) = raw(0x1B, 0x21, if (on) 0x10 else 0x00)
        fun cut()                   = raw(0x1D, 0x56, 0x01)      // Partial cut
        fun divider(ch: Char = '-') = text(ch.toString().repeat(LINE_WIDTH))

        fun paddedLine(leftText: String, rightText: String): String {
            val spaces = LINE_WIDTH - leftText.length - rightText.length
            return if (spaces <= 0) "$leftText $rightText"
            else leftText + " ".repeat(spaces) + rightText
        }

        fun rupiah(amount: Long): String {
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

        // ── Header ──────────────────────────────────────────────────────────
        init()
        center()
        bold(true); doubleHeight(true)
        text(data.storeName.uppercase()); lf()
        doubleHeight(false); bold(false)

        if (!data.storeAddress.isNullOrBlank()) { text(data.storeAddress); lf() }
        if (!data.storePhone.isNullOrBlank())   { text(data.storePhone);   lf() }
        lf()

        // ── Invoice header ───────────────────────────────────────────────────
        left()
        divider('='); lf()
        text(paddedLine("#${data.invoiceNo}", data.orderType.uppercase())); lf()
        if (!data.tableName.isNullOrBlank())    { text("Meja  : ${data.tableName}");   lf() }
        if (!data.cashierName.isNullOrBlank())  { text("Kasir : ${data.cashierName}"); lf() }
        text(data.createdAt); lf()
        divider(); lf()

        // ── Items ────────────────────────────────────────────────────────────
        for (item in data.items) {
            val nameDisplay = if (!item.variantName.isNullOrBlank())
                "${item.name} (${item.variantName})"
            else item.name

            // Truncate name to fit (leave 10 chars for subtotal)
            val maxName = LINE_WIDTH - 10
            val truncated = if (nameDisplay.length > maxName)
                nameDisplay.substring(0, maxName - 1) + "…"
            else nameDisplay

            text(truncated); lf()
            val qtyPrice = "  ${item.qty}x ${rupiah(item.price)}"
            text(paddedLine(qtyPrice, rupiah(item.subtotal))); lf()
            if (!item.note.isNullOrBlank()) { text("  *${item.note}"); lf() }
        }

        divider(); lf()

        // ── Totals ───────────────────────────────────────────────────────────
        text(paddedLine("Subtotal", rupiah(data.subtotal))); lf()
        if (data.discount   > 0) { text(paddedLine("Diskon",   "-${rupiah(data.discount)}"));     lf() }
        if (data.surcharge  > 0) { text(paddedLine("Surcharge", rupiah(data.surcharge)));         lf() }
        if (data.tax        > 0) { text(paddedLine("Pajak",     rupiah(data.tax)));               lf() }
        if (data.deliveryFee > 0) { text(paddedLine("Ongkir",   rupiah(data.deliveryFee)));       lf() }
        if (data.tip        > 0) { text(paddedLine("Tip",       rupiah(data.tip)));               lf() }

        divider('='); lf()
        bold(true); doubleHeight(true)
        text(paddedLine("TOTAL", rupiah(data.total))); lf()
        doubleHeight(false); bold(false)

        if (!data.paymentMethod.isNullOrBlank()) {
            val method = data.paymentMethod.uppercase()
            text(paddedLine("Bayar ($method)", rupiah(data.paidAmount))); lf()
            if (data.changeAmount > 0) {
                text(paddedLine("Kembalian", rupiah(data.changeAmount))); lf()
            }
        }

        divider('='); lf()

        // ── Footer ───────────────────────────────────────────────────────────
        center(); lf()
        text("Terima Kasih!"); lf()
        text("Sampai jumpa kembali :)"); lf()
        lf(); lf(); lf()

        cut()

        return buf.toByteArray()
    }
}
