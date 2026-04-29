package id.rancak.app.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A read-only [OutlinedTextField] that opens a Material3 [TimePickerDialog] on tap.
 *
 * @param value  Time string "HH:mm", or "" for no selection.
 * @param onTimeSelected  Called with "HH:mm" string when the user confirms.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerField(
    label: String,
    value: String,
    onTimeSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null
) {
    var showPicker by remember { mutableStateOf(false) }

    val initialHour   = remember(value) { value.substringBefore(':', "0").toIntOrNull() ?: 0 }
    val initialMinute = remember(value) { value.substringAfter(':', "0").toIntOrNull() ?: 0 }

    Box(modifier = modifier) {
        OutlinedTextField(
            value         = value,
            onValueChange = {},
            readOnly      = true,
            label         = { Text(label) },
            trailingIcon  = { Icon(Icons.Default.Schedule, contentDescription = "Pilih waktu") },
            isError       = isError,
            supportingText = supportingText,
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = { showPicker = true }
                )
        )
    }

    if (showPicker) {
        val state = rememberTimePickerState(
            initialHour   = initialHour,
            initialMinute = initialMinute,
            is24Hour      = true
        )
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title   = { Text("Pilih Waktu") },
            text    = {
                Column(
                    modifier          = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimePicker(
                        state    = state,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val hh = state.hour.toString().padStart(2, '0')
                    val mm = state.minute.toString().padStart(2, '0')
                    onTimeSelected("$hh:$mm")
                    showPicker = false
                }) { Text("Pilih") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Batal") }
            }
        )
    }
}
