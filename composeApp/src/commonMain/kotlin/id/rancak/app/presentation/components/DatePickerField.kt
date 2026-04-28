package id.rancak.app.presentation.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import id.rancak.app.presentation.designsystem.RancakTheme
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

/**
 * A read-only [OutlinedTextField] that opens a Material3 [DatePickerDialog] on tap.
 *
 * @param value  ISO date string "YYYY-MM-DD", or "" for no selection.
 * @param onDateSelected  Called with an ISO "YYYY-MM-DD" string when the user confirms.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    value: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null
) {
    var showPicker by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed) showPicker = true
    }

    OutlinedTextField(
        value = if (value.length == 10) formatDateMedium(value) else value,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = {
            Icon(Icons.Default.DateRange, contentDescription = "Pilih tanggal")
        },
        interactionSource = interactionSource,
        isError = isError,
        supportingText = supportingText,
        singleLine = true,
        modifier = modifier
    )

    if (showPicker) {
        val initialMillis = remember(value) {
            if (value.length == 10) {
                try {
                    LocalDate.parse(value).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
                } catch (_: Exception) {
                    null
                }
            } else null
        }
        val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { millis ->
                            val dateStr = Instant.fromEpochMilliseconds(millis)
                                .toLocalDateTime(TimeZone.UTC).date.toString()
                            onDateSelected(dateStr)
                        }
                        showPicker = false
                    },
                    enabled = state.selectedDateMillis != null
                ) { Text("Pilih") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Batal") }
            }
        ) {
            DatePicker(state = state)
        }
    }
}

/** Convert "YYYY-MM-DD" to a friendly "15 Apr 2024" display label. */
private fun formatDateMedium(dateStr: String): String {
    val p = dateStr.split("-")
    if (p.size != 3) return dateStr
    val day = p[2].trimStart('0').ifEmpty { "0" }
    val month = when (p[1].toIntOrNull()) {
        1 -> "Jan"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Apr"
        5 -> "Mei"; 6 -> "Jun"; 7 -> "Jul"; 8 -> "Agu"
        9 -> "Sep"; 10 -> "Okt"; 11 -> "Nov"; 12 -> "Des"
        else -> p[1]
    }
    return "$day $month ${p[0]}"
}

@Preview
@Composable
private fun DatePickerFieldPreview() {
    RancakTheme {
        DatePickerField(
            label = "Tanggal",
            value = "2024-04-15",
            onDateSelected = {}
        )
    }
}
