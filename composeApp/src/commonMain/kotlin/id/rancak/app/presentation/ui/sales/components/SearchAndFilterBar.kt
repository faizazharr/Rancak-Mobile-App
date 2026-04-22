package id.rancak.app.presentation.ui.sales.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.SaleStatus
import id.rancak.app.presentation.designsystem.RancakColors
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.ui.sales.formatDateShort
import id.rancak.app.presentation.viewmodel.DateFilter
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Top bar of [id.rancak.app.presentation.ui.sales.SalesHistoryScreen]: a
 * search field plus horizontally-scrolling date-preset and status chips.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SearchAndFilterBar(
    query: String,
    dateFilter: DateFilter,
    statusFilter: SaleStatus?,
    customDateFrom: String?,
    customDateTo: String?,
    onQueryChange: (String) -> Unit,
    onDateFilter: (DateFilter) -> Unit,
    onStatusFilter: (SaleStatus?) -> Unit,
    onCustomDateRange: (Long, Long) -> Unit,
    onClear: () -> Unit,
    hasActiveFilter: Boolean
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth().height(46.dp),
            placeholder = {
                Text(
                    "Cari invoice atau nama produk…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
                AnimatedVisibility(visible = query.isNotBlank(), enter = fadeIn(), exit = fadeOut()) {
                    IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Hapus", modifier = Modifier.size(16.dp))
                    }
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            textStyle = MaterialTheme.typography.bodySmall
        )

        DateFilterRow(
            dateFilter     = dateFilter,
            customDateFrom = customDateFrom,
            customDateTo   = customDateTo,
            onDateFilter   = onDateFilter,
            onPickCustom   = { showDatePicker = true },
            onClear        = onClear
        )

        StatusFilterRow(
            selected        = statusFilter,
            hasActiveFilter = hasActiveFilter,
            onStatusFilter  = onStatusFilter,
            onClear         = onClear
        )
    }

    if (showDatePicker) {
        DateRangePickerDialog(
            onDismiss = { showDatePicker = false },
            onConfirm = { from, to ->
                onCustomDateRange(from, to)
                showDatePicker = false
            }
        )
    }
}

@Composable
private fun DateFilterRow(
    dateFilter: DateFilter,
    customDateFrom: String?,
    customDateTo: String?,
    onDateFilter: (DateFilter) -> Unit,
    onPickCustom: () -> Unit,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        DateFilter.entries
            .filter { it != DateFilter.CUSTOM }
            .forEach { filter ->
                DatePill(
                    label    = filter.label,
                    selected = dateFilter == filter,
                    onClick  = { onDateFilter(filter) }
                )
            }

        val customLabel = if (customDateFrom != null && customDateTo != null)
            "${formatDateShort(customDateFrom)} – ${formatDateShort(customDateTo)}"
        else null

        CustomDatePill(
            label    = customLabel,
            selected = dateFilter == DateFilter.CUSTOM,
            onClick  = onPickCustom,
            onClear  = {
                onDateFilter(DateFilter.ALL)
                onClear()
            }
        )
    }
}

@Composable
private fun StatusFilterRow(
    selected: SaleStatus?,
    hasActiveFilter: Boolean,
    onStatusFilter: (SaleStatus?) -> Unit,
    onClear: () -> Unit
) {
    val statusOptions: List<Pair<SaleStatus?, String>> = listOf(
        null                to "Semua",
        SaleStatus.HELD     to "Belum Bayar",
        SaleStatus.PAID     to "Lunas",
        SaleStatus.REFUNDED to "Refund",
        SaleStatus.VOID     to "Void"
    )
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        statusOptions.forEach { (status, label) ->
            StatusPill(
                label    = label,
                status   = status,
                selected = selected == status,
                onClick  = { onStatusFilter(status) }
            )
        }

        if (hasActiveFilter) {
            Spacer(Modifier.width(4.dp))
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                modifier = Modifier
                    .height(28.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable(onClick = onClear)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.FilterAltOff, contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        "Reset",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/** Pill for preset date filters (Hari Ini / Kemarin / 7 Hari / …). */
@Composable
private fun DatePill(label: String, selected: Boolean, onClick: () -> Unit) {
    val bgColor = if (selected) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    val txtColor = if (selected) MaterialTheme.colorScheme.onPrimary
                   else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = bgColor,
        modifier = Modifier
            .height(30.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable(onClick = onClick)
            .then(
                if (!selected) Modifier.border(
                    width = 0.8.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    shape = MaterialTheme.shapes.extraLarge
                ) else Modifier
            )
    ) {
        Box(modifier = Modifier.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = txtColor
            )
        }
    }
}

/** Custom date-range pill — shows the picked range or "Pilih Tanggal" hint. */
@Composable
private fun CustomDatePill(
    label: String?,
    selected: Boolean,
    onClick: () -> Unit,
    onClear: () -> Unit
) {
    val bgColor = if (selected) MaterialTheme.colorScheme.tertiary
                  else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    val txtColor = if (selected) MaterialTheme.colorScheme.onTertiary
                   else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = bgColor,
        modifier = Modifier
            .height(30.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable(onClick = onClick)
            .then(
                if (!selected) Modifier.border(
                    width = 0.8.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    shape = MaterialTheme.shapes.extraLarge
                ) else Modifier
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(12.dp), tint = txtColor)
            Text(
                text = label ?: "Pilih Tanggal",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = txtColor
            )
            if (selected && label != null) {
                Spacer(Modifier.width(2.dp))
                Icon(
                    Icons.Default.Close, contentDescription = "Hapus rentang",
                    modifier = Modifier.size(12.dp).clickable(onClick = onClear),
                    tint = txtColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}

/** Status pill (Lunas, Belum Bayar, Void, Refund) with a coloured dot. */
@Composable
private fun StatusPill(
    label: String,
    status: SaleStatus?,
    selected: Boolean,
    onClick: () -> Unit
) {
    val semantic = RancakColors.semantic
    val dotColor = when (status) {
        SaleStatus.PAID      -> semantic.success
        SaleStatus.HELD      -> semantic.warning
        SaleStatus.VOID,
        SaleStatus.CANCELLED -> MaterialTheme.colorScheme.error
        SaleStatus.REFUNDED  -> semantic.info
        null                 -> MaterialTheme.colorScheme.outline
    }
    val bgColor     = if (selected) dotColor.copy(alpha = 0.15f)
                      else MaterialTheme.colorScheme.surface
    val borderColor = if (selected) dotColor
                      else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val txtColor    = if (selected) dotColor
                      else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = bgColor,
        modifier = Modifier
            .height(28.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable(onClick = onClick)
            .border(
                width = if (selected) 1.2.dp else 0.6.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.extraLarge
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (status != null) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(dotColor)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = txtColor
            )
        }
    }
}

/** Material3 date-range picker in a dialog. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Long, Long) -> Unit
) {
    val state = rememberDateRangePickerState()
    val confirmEnabled = state.selectedStartDateMillis != null &&
        state.selectedEndDateMillis != null

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(state.selectedStartDateMillis!!, state.selectedEndDateMillis!!)
                },
                enabled = confirmEnabled
            ) { Text("Terapkan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    ) {
        DateRangePicker(
            state = state,
            title = {
                Text(
                    "Pilih Rentang Tanggal",
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp)
                )
            },
            headline = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = state.selectedStartDateMillis?.let {
                            formatDateShort(
                                Instant.fromEpochMilliseconds(it)
                                    .toLocalDateTime(TimeZone.UTC).date.toString()
                            )
                        } ?: "Mulai",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (state.selectedStartDateMillis != null)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("–", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = state.selectedEndDateMillis?.let {
                            formatDateShort(
                                Instant.fromEpochMilliseconds(it)
                                    .toLocalDateTime(TimeZone.UTC).date.toString()
                            )
                        } ?: "Selesai",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (state.selectedEndDateMillis != null)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            modifier = Modifier.weight(1f, false)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun SearchAndFilterBarPreview_Empty() {
    RancakTheme {
        SearchAndFilterBar(
            query             = "",
            dateFilter        = DateFilter.ALL,
            statusFilter      = null,
            customDateFrom    = null,
            customDateTo      = null,
            onQueryChange     = {},
            onDateFilter      = {},
            onStatusFilter    = {},
            onCustomDateRange = { _, _ -> },
            onClear           = {},
            hasActiveFilter   = false
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun SearchAndFilterBarPreview_Active() {
    RancakTheme {
        SearchAndFilterBar(
            query             = "INV-0001",
            dateFilter        = DateFilter.TODAY,
            statusFilter      = SaleStatus.PAID,
            customDateFrom    = null,
            customDateTo      = null,
            onQueryChange     = {},
            onDateFilter      = {},
            onStatusFilter    = {},
            onCustomDateRange = { _, _ -> },
            onClear           = {},
            hasActiveFilter   = true
        )
    }
}
