package id.rancak.app.presentation.ui.reports

import androidx.compose.ui.graphics.Color
import kotlin.time.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

/** Periode laporan yang bisa dipilih dari [PeriodSelectorRow]. */
internal enum class ReportPeriod(val label: String) {
    TODAY("Hari Ini"),
    WEEK("7 Hari"),
    THIS_MONTH("Bulan Ini"),
    LAST_MONTH("Bulan Lalu"),
    CUSTOM("Pilih")
}

/**
 * Hitung rentang tanggal (from, to) untuk periode ini.
 * Return `null` untuk [ReportPeriod.CUSTOM] — user harus memilih manual.
 */
@Suppress("DEPRECATION") // kotlinx.datetime LocalDate.monthNumber
internal fun ReportPeriod.toDateRange(): Pair<String, String>? {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    return when (this) {
        ReportPeriod.TODAY ->
            today.toString() to today.toString()

        ReportPeriod.WEEK ->
            today.minus(DatePeriod(days = 6)).toString() to today.toString()

        ReportPeriod.THIS_MONTH -> {
            val from = LocalDate(today.year, today.monthNumber, 1)
            from.toString() to today.toString()
        }

        ReportPeriod.LAST_MONTH -> {
            val firstThisMonth = LocalDate(today.year, today.monthNumber, 1)
            val firstLastMonth = firstThisMonth.minus(DatePeriod(months = 1))
            val lastLastMonth  = firstThisMonth.minus(DatePeriod(days = 1))
            firstLastMonth.toString() to lastLastMonth.toString()
        }

        ReportPeriod.CUSTOM -> null
    }
}

/** Palette warna yang dipakai donut chart / legenda pembayaran. */
internal val chartColors: List<Color> = listOf(
    Color(0xFF4CAF50),
    Color(0xFF2196F3),
    Color(0xFFFF9800),
    Color(0xFF9C27B0),
    Color(0xFF00BCD4),
    Color(0xFFE91E63),
    Color(0xFF795548)
)
