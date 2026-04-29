package id.rancak.app.presentation.ui.reservations.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import id.rancak.app.domain.model.Reservation
import id.rancak.app.domain.model.ReservationInput
import id.rancak.app.domain.model.Table
import id.rancak.app.domain.model.TableStatus
import id.rancak.app.presentation.components.DatePickerField
import id.rancak.app.presentation.components.TimePickerField

/**
 * Form reservasi full-screen agar DatePickerField dan TimePickerField
 * punya cukup ruang horizontal (kiri-kanan).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationFormDialog(
    editing: Reservation?,
    tables: List<Table>,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (ReservationInput) -> Unit
) {
    var customerName  by remember(editing) { mutableStateOf(editing?.customerName ?: "") }
    var customerPhone by remember(editing) { mutableStateOf(editing?.customerPhone ?: "") }
    var partySizeStr  by remember(editing) { mutableStateOf((editing?.partySize ?: 2).toString()) }
    var reservedDate  by remember(editing) { mutableStateOf(editing?.reservedAt?.substringBefore('T') ?: "") }
    var reservedTime  by remember(editing) { mutableStateOf(editing?.reservedAt?.substringAfter('T', "")?.take(5) ?: "") }
    var durationStr   by remember(editing) { mutableStateOf((editing?.durationMinutes ?: 90).toString()) }
    var note          by remember(editing) { mutableStateOf(editing?.note ?: "") }
    var tableUuid: String? by remember(editing) { mutableStateOf(editing?.tableUuid) }
    var tableMenuOpen by remember { mutableStateOf(false) }

    val partySize  = partySizeStr.toIntOrNull() ?: 0
    val duration   = durationStr.toIntOrNull() ?: 0
    val canConfirm = !isSubmitting &&
        customerName.isNotBlank() &&
        partySize >= 1 &&
        reservedDate.isNotBlank() &&
        reservedTime.isNotBlank() &&
        duration >= 1

    val selectedTable = remember(tableUuid, tables) {
        tables.firstOrNull { it.uuid == tableUuid }
    }

    Dialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress      = !isSubmitting,
            dismissOnClickOutside   = false
        )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            if (editing == null) "Reservasi Baru" else "Edit Reservasi",
                            style      = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss, enabled = !isSubmitting) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Tutup")
                        }
                    },
                    actions = {
                        Button(
                            onClick = {
                                onConfirm(
                                    ReservationInput(
                                        customerName    = customerName.trim(),
                                        customerPhone   = customerPhone.ifBlank { null }?.trim(),
                                        partySize       = partySize,
                                        reservedAt      = "${reservedDate.trim()}T${reservedTime.trim()}",
                                        durationMinutes = duration,
                                        tableUuid       = tableUuid,
                                        note            = note.ifBlank { null }?.trim()
                                    )
                                )
                            },
                            enabled  = canConfirm,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            if (isSubmitting)
                                CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                            else
                                Text("Simpan")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Seksi 1: Tamu ─────────────────────────────────────────
                SectionLabel("Informasi Tamu", Icons.Default.Person)

                OutlinedTextField(
                    value          = customerName,
                    onValueChange  = { customerName = it },
                    label          = { Text("Nama Tamu *") },
                    placeholder    = { Text("Nama lengkap tamu") },
                    singleLine     = true,
                    isError        = customerName.isBlank(),
                    leadingIcon    = { Icon(Icons.Default.Person, null, modifier = Modifier.size(18.dp)) },
                    supportingText = if (customerName.isBlank()) {{ Text("Wajib diisi") }} else null,
                    modifier       = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value           = customerPhone,
                    onValueChange   = { customerPhone = it },
                    label           = { Text("Nomor Telepon") },
                    placeholder     = { Text("08xxx") },
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    leadingIcon     = { Icon(Icons.Default.Phone, null, modifier = Modifier.size(18.dp)) },
                    modifier        = Modifier.fillMaxWidth()
                )

                // ── Seksi 2: Jadwal ───────────────────────────────────────
                SectionLabel("Jadwal", Icons.Default.Event)

                // Tanggal (kiri) + Jam (kanan) — ruang penuh layar
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment     = Alignment.Top,
                    modifier              = Modifier.fillMaxWidth()
                ) {
                    DatePickerField(
                        label          = "Tanggal *",
                        value          = reservedDate,
                        onDateSelected = { reservedDate = it },
                        isError        = reservedDate.isBlank(),
                        modifier       = Modifier.weight(1.6f)
                    )
                    TimePickerField(
                        label          = "Jam *",
                        value          = reservedTime,
                        onTimeSelected = { reservedTime = it },
                        isError        = reservedTime.isBlank(),
                        modifier       = Modifier.weight(1f)
                    )
                }

                // Jumlah tamu (kiri) + Durasi (kanan)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment     = Alignment.Top,
                    modifier              = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value           = partySizeStr,
                        onValueChange   = { partySizeStr = it.filter { c -> c.isDigit() } },
                        label           = { Text("Jumlah Tamu *") },
                        suffix          = { Text("orang") },
                        singleLine      = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError         = partySize < 1,
                        leadingIcon     = { Icon(Icons.Default.Group, null, modifier = Modifier.size(18.dp)) },
                        modifier        = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value           = durationStr,
                        onValueChange   = { durationStr = it.filter { c -> c.isDigit() } },
                        label           = { Text("Durasi *") },
                        suffix          = { Text("menit") },
                        singleLine      = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError         = duration < 1,
                        leadingIcon     = { Icon(Icons.Default.Timer, null, modifier = Modifier.size(18.dp)) },
                        modifier        = Modifier.weight(1f)
                    )
                }

                // ── Seksi 3: Meja & Catatan ───────────────────────────────
                SectionLabel("Meja & Catatan", Icons.Default.TableBar)

                ExposedDropdownMenuBox(
                    expanded         = tableMenuOpen,
                    onExpandedChange = { tableMenuOpen = !tableMenuOpen }
                ) {
                    OutlinedTextField(
                        value         = selectedTable
                            ?.let { "${it.name}${it.area?.let { a -> " · $a" } ?: ""}" }
                            ?: "— Tanpa meja —",
                        onValueChange = {},
                        readOnly      = true,
                        label         = { Text("Pilih Meja (opsional)") },
                        leadingIcon   = { Icon(Icons.Default.TableBar, null, modifier = Modifier.size(18.dp)) },
                        trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tableMenuOpen) },
                        modifier      = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded         = tableMenuOpen,
                        onDismissRequest = { tableMenuOpen = false }
                    ) {
                        DropdownMenuItem(
                            text    = { Text("— Tanpa meja —") },
                            onClick = { tableUuid = null; tableMenuOpen = false }
                        )
                        tables.filter { it.status != TableStatus.INACTIVE }.forEach { t ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("${t.name}${t.area?.let { a -> " · $a" } ?: ""}")
                                        Text(
                                            "${t.capacity} kursi",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = { tableUuid = t.uuid; tableMenuOpen = false }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value         = note,
                    onValueChange = { note = it },
                    label         = { Text("Catatan") },
                    placeholder   = { Text("Preferensi tempat duduk, alergi, dll.") },
                    maxLines      = 3,
                    leadingIcon   = { Icon(Icons.Default.Notes, null, modifier = Modifier.size(18.dp)) },
                    modifier      = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Icon(
            icon, contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.primaryContainer)
}
