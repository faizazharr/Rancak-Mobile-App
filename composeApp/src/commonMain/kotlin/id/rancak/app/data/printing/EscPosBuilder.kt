package id.rancak.app.data.printing

import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.core.toByteArray

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

    // ── Shared helper functions ─────────────────────────────────────────────

    private fun MutableList<Byte>.raw(vararg bytes: Int) =
        bytes.forEach { add(it.toByte()) }

    private fun MutableList<Byte>.text(s: String) =
        s.toByteArray(Charsets.UTF_8).forEach { add(it) }

    private fun MutableList<Byte>.lf() = add(0x0A.toByte())
    private fun MutableList<Byte>.init() = raw(0x1B, 0x40)
    private fun MutableList<Byte>.center() = raw(0x1B, 0x61, 0x01)
    private fun MutableList<Byte>.left() = raw(0x1B, 0x61, 0x00)
    private fun MutableList<Byte>.bold(on: Boolean) = raw(0x1B, 0x45, if (on) 0x01 else 0x00)
    private fun MutableList<Byte>.doubleHeight(on: Boolean) = raw(0x1B, 0x21, if (on) 0x10 else 0x00)
    private fun MutableList<Byte>.doubleSize(on: Boolean) = raw(0x1B, 0x21, if (on) 0x30 else 0x00)
    private fun MutableList<Byte>.cut() = raw(0x1D, 0x56, 0x01)
    private fun MutableList<Byte>.divider(ch: Char = '-') = text(ch.toString().repeat(LINE_WIDTH))

    private fun paddedLine(leftText: String, rightText: String): String {
        val spaces = LINE_WIDTH - leftText.length - rightText.length
        return if (spaces <= 0) "$leftText $rightText"
        else leftText + " ".repeat(spaces) + rightText
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

    // ── Cashier Receipt ─────────────────────────────────────────────────────

    fun buildReceipt(data: ReceiptData): ByteArray {
        val buf = mutableListOf<Byte>()

        // ── Header ──────────────────────────────────────────────────────────
        buf.init()
        buf.center()
        buf.bold(true); buf.doubleHeight(true)
        buf.text(data.storeName.uppercase()); buf.lf()
        buf.doubleHeight(false); buf.bold(false)

        if (!data.storeAddress.isNullOrBlank()) { buf.text(data.storeAddress); buf.lf() }
        if (!data.storePhone.isNullOrBlank())   { buf.text(data.storePhone);   buf.lf() }
        buf.lf()

        // ── Invoice header ───────────────────────────────────────────────────
        buf.left()
        buf.divider('='); buf.lf()
        buf.text(paddedLine("#${data.invoiceNo}", data.orderType.uppercase())); buf.lf()
        if (!data.tableName.isNullOrBlank())    { buf.text("Meja  : ${data.tableName}");   buf.lf() }
        if (!data.cashierName.isNullOrBlank())  { buf.text("Kasir : ${data.cashierName}"); buf.lf() }
        buf.text(data.createdAt); buf.lf()
        buf.divider(); buf.lf()

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

            buf.text(truncated); buf.lf()
            val qtyPrice = "  ${item.qty}x ${rupiah(item.price)}"
            buf.text(paddedLine(qtyPrice, rupiah(item.subtotal))); buf.lf()
            if (!item.note.isNullOrBlank()) { buf.text("  *${item.note}"); buf.lf() }
        }

        buf.divider(); buf.lf()

        // ── Totals ───────────────────────────────────────────────────────────
        buf.text(paddedLine("Subtotal", rupiah(data.subtotal))); buf.lf()
        if (data.discount   > 0) { buf.text(paddedLine("Diskon",   "-${rupiah(data.discount)}"));     buf.lf() }
        if (data.surcharge  > 0) { buf.text(paddedLine("Surcharge", rupiah(data.surcharge)));         buf.lf() }
        if (data.tax        > 0) { buf.text(paddedLine("Pajak",     rupiah(data.tax)));               buf.lf() }
        if (data.deliveryFee > 0) { buf.text(paddedLine("Ongkir",   rupiah(data.deliveryFee)));       buf.lf() }
        if (data.tip        > 0) { buf.text(paddedLine("Tip",       rupiah(data.tip)));               buf.lf() }

        buf.divider('='); buf.lf()
        buf.bold(true); buf.doubleHeight(true)
        buf.text(paddedLine("TOTAL", rupiah(data.total))); buf.lf()
        buf.doubleHeight(false); buf.bold(false)

        if (!data.paymentMethod.isNullOrBlank()) {
            val method = data.paymentMethod.uppercase()
            buf.text(paddedLine("Bayar ($method)", rupiah(data.paidAmount))); buf.lf()
            if (data.changeAmount > 0) {
                buf.text(paddedLine("Kembalian", rupiah(data.changeAmount))); buf.lf()
            }
        }

        buf.divider('='); buf.lf()

        // ── Footer ───────────────────────────────────────────────────────────
        buf.center(); buf.lf()
        val footer = data.footerText ?: "Terima Kasih!"
        buf.text(footer); buf.lf()
        if (data.footerText == null) {
            buf.text("Sampai jumpa kembali :)"); buf.lf()
        }
        buf.lf(); buf.lf(); buf.lf()

        buf.cut()

        return buf.toByteArray()
    }

    // ── Kitchen Order Ticket (KOT) ──────────────────────────────────────────

    /**
     * Builds ESC/POS bytes for a Kitchen Order Ticket.
     * - No prices or totals
     * - Item names in DOUBLE-HEIGHT font for readability
     * - Table / queue number prominently displayed
     * - Notes highlighted with >> prefix
     */
    fun buildKitchenTicket(data: KitchenTicketData): ByteArray {
        val buf = mutableListOf<Byte>()

        buf.init()

        // ── Header: ★ DAPUR / KITCHEN ★ ─────────────────────────────────────
        buf.center()
        buf.bold(true); buf.doubleHeight(true)
        buf.text("DAPUR / KITCHEN"); buf.lf()
        buf.doubleHeight(false)
        buf.text(data.storeName.uppercase()); buf.lf()
        buf.bold(false)

        buf.divider('='); buf.lf()

        // ── Order info ───────────────────────────────────────────────────────
        buf.left()
        buf.text("No : ${data.invoiceNo}"); buf.lf()
        buf.text("Tgl: ${data.createdAt}"); buf.lf()
        buf.bold(true)
        buf.text("Tipe: ${data.orderType.uppercase()}"); buf.lf()
        buf.bold(false)

        // ── Table or queue number (large) ────────────────────────────────────
        if (!data.tableName.isNullOrBlank()) {
            buf.center()
            buf.bold(true); buf.doubleSize(true)
            buf.text("Meja: ${data.tableName}"); buf.lf()
            buf.doubleSize(false); buf.bold(false)
            buf.left()
        } else if (data.queueNumber != null) {
            buf.center()
            buf.bold(true); buf.doubleSize(true)
            buf.text("#${data.queueNumber}"); buf.lf()
            buf.doubleSize(false); buf.bold(false)
            buf.left()
            buf.center(); buf.text("Antrian"); buf.lf(); buf.left()
        }

        if (!data.customerName.isNullOrBlank()) {
            buf.text("Pelanggan: ${data.customerName}"); buf.lf()
        }
        if (!data.cashierName.isNullOrBlank()) {
            buf.text("Kasir: ${data.cashierName}"); buf.lf()
        }

        buf.divider('='); buf.lf()

        // ── Items (double-height names, qty highlighted) ─────────────────────
        for (item in data.items) {
            buf.bold(true); buf.doubleHeight(true)
            buf.text(item.name); buf.lf()
            buf.doubleHeight(false); buf.bold(false)

            buf.bold(true)
            buf.text("  x${item.qty}"); buf.lf()
            buf.bold(false)

            if (!item.note.isNullOrBlank()) {
                buf.text("  >> ${item.note}"); buf.lf()
            }

            buf.lf() // spacing between items
        }

        buf.divider('='); buf.lf()

        // ── Footer ───────────────────────────────────────────────────────────
        buf.center()
        buf.text("-- SELESAI / DONE --"); buf.lf()
        buf.lf(); buf.lf(); buf.lf()

        buf.cut()

        return buf.toByteArray()
    }

    // ── Combined Receipt (single printer — KOT + Cashier) ───────────────────

    /**
     * Builds a combined byte stream for a single printer:
     * both KOT and cashier receipt separated by a paper cut.
     *
     * @param kotFirst If true (default), KOT prints first then cashier receipt.
     *                 If false, cashier receipt prints first then KOT.
     */
    fun buildCombinedReceipt(
        receiptData: ReceiptData,
        kitchenData: KitchenTicketData,
        kotFirst: Boolean = true
    ): ByteArray {
        val receiptBytes = buildReceipt(receiptData)
        val kotBytes = buildKitchenTicket(kitchenData)
        return if (kotFirst) kotBytes + receiptBytes else receiptBytes + kotBytes
    }
}
