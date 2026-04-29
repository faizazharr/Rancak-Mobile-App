package id.rancak.app.presentation.ui.reservations.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
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
import id.rancak.app.domain.model.Reservation
import id.rancak.app.domain.model.ReservationInput
import id.rancak.app.domain.model.Table
import id.rancak.app.domain.model.TableStatus
import id.rancak.app.presentation.components.DatePickerField
import id.rancak.app.presentation.components.TimePickerField

// ─────────────────────────────────────────────────────────────────────────────
// Phone: form full-screen (Scaffold + TopAppBar)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationFormContent(
    editing: Reservation?,
    tables: List<Table>,
    isSubmitting: Boolean,
    onBack: () -> Unit,
    onConfirm: (ReservationInput) -> Unit
) {
    val state = rememberReservationFormState(editing)

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
                    IconButton(onClick = onBack, enabled = !isSubmitting) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    Button(
                        onClick  = { onConfirm(state.toInput()) },
                        enabled  = state.canConfirm(!isSubmitting),
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
        ReservationFormFields(
            state   = state,
            tables  = tables,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tablet: form panel (no Scaffold, rendered in right pane of split layout)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ReservationFormPanel(
    editing: Reservation?,
    tables: List<Table>,
    isSubmitting: Boolean,
    onClose: () -> Unit,
    onConfirm: (ReservationInput) -> Unit
) {
    val state = rememberReservationFormState(editing)

    Column(Modifier.fillMaxSize()) {
        // ── Panel header ──────────────────────────────────────────────────
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                if (editing == null) "Reservasi Baru" else "Edit Reservasi",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onClose, enabled = !isSubmitting) { Text("Batal") }
                Button(
                    onClick  = { onConfirm(state.toInput()) },
                    enabled  = state.canConfirm(!isSubmitting)
                ) {
                    if (isSubmitting)
                        CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp)
                    else
                        Text("Simpan")
                }
                IconButton(onClick = onClose, enabled = !isSubmitting) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        HorizontalDivider()
        ReservationFormFields(
            state    = state,
            tables   = tables,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared form fields
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReservationFormFields(
    state: ReservationFormMutableState,
    tables: List<Table>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {

        // ── Seksi 1: Tamu ─────────────────────────────────────────────────
        SectionLabel("Informasi Tamu", Icons.Default.Person)

        OutlinedTextField(
            value          = state.customerName,
            onValueChange  = { state.customerName = it },
            label          = { Text("Nama Tamu *") },
            placeholder    = { Text("Nama lengkap tamu") },
            singleLine     = true,
            isError        = state.customerName.isBlank(),
            leadingIcon    = { Icon(Icons.Default.Person, null, modifier = Modifier.size(18.dp)) },
            supportingText = if (state.customerName.isBlank()) {{ Text("Wajib diisi") }} else null,
            modifier       = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value           = state.customerPhone,
            onValueChange   = { state.customerPhone = it },
            label           = { Text("Nomor Telepon") },
            placeholder     = { Text("08xxx") },
            singleLine      = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            leadingIcon     = { Icon(Icons.Default.Phone, null, modifier = Modifier.size(18.dp)) },
            modifier        = Modifier.fillMaxWidth()
        )

        // ── Seksi 2: Jadwal ───────────────────────────────────────────────
        SectionLabel("Jadwal", Icons.Default.Event)

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment     = Alignment.Top,
            modifier              = Modifier.fillMaxWidth()
        ) {
            DatePickerField(
                label          = "Tanggal *",
                value          = state.reservedDate,
                onDateSelected = { state.reservedDate = it },
                isError        = state.reservedDate.isBlank(),
                modifier       = Modifier.weight(1.6f)
            )
            TimePickerField(
                label          = "Jam *",
                value          = state.reservedTime,
                onTimeSelected = { state.reservedTime = it },
                isError        = state.reservedTime.isBlank(),
                modifier       = Modifier.weight(1f)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment     = Alignment.Top,
            modifier              = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value           = state.partySizeStr,
                onValueChange   = { state.partySizeStr = it.filter { c -> c.isDigit() } },
                label           = { Text("Jumlah Tamu *") },
                suffix          = { Text("orang") },
                singleLine      = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError         = (state.partySizeStr.toIntOrNull() ?: 0) < 1,
                leadingIcon     = { Icon(Icons.Default.Group, null, modifier = Modifier.size(18.dp)) },
                modifier        = Modifier.weight(1f)
            )
            OutlinedTextField(
                value           = state.durationStr,
                onValueChange   = { state.durationStr = it.filter { c -> c.isDigit() } },
                label           = { Text("Durasi *") },
                suffix          = { Text("menit") },
                singleLine      = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError         = (state.durationStr.toIntOrNull() ?: 0) < 1,
                leadingIcon     = { Icon(Icons.Default.Timer, null, modifier = Modifier.size(18.dp)) },
                modifier        = Modifier.weight(1f)
            )
        }

        // ── Seksi 3: Meja & Catatan ───────────────────────────────────────
        SectionLabel("Meja & Catatan", Icons.Default.TableBar)

        val areas = remember(tables) {
            tables
                .filter { it.status != TableStatus.INACTIVE }
                .mapNotNull { it.area }
                .distinct()
                .sorted()
        }
        val activeTables = remember(tables, state.selectedArea) {
            tables
                .filter { it.status != TableStatus.INACTIVE }
                .let { list ->
                    if (state.selectedArea == null) list
                    else list.filter { it.area == state.selectedArea }
                }
        }
        val selectedTable = remember(state.tableUuid, tables) {
            tables.firstOrNull { it.uuid == state.tableUuid }
        }

        // Area filter — only shown when there are multiple areas
        if (areas.size > 1) {
            ExposedDropdownMenuBox(
                expanded         = state.areaMenuOpen,
                onExpandedChange = { state.areaMenuOpen = !state.areaMenuOpen }
            ) {
                OutlinedTextField(
                    value         = state.selectedArea ?: "Semua Area",
                    onValueChange = {},
                    readOnly      = true,
                    label         = { Text("Filter Area") },
                    leadingIcon   = { Icon(Icons.Default.TableBar, null, modifier = Modifier.size(18.dp)) },
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.areaMenuOpen) },
                    modifier      = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded         = state.areaMenuOpen,
                    onDismissRequest = { state.areaMenuOpen = false }
                ) {
                    DropdownMenuItem(
                        text    = { Text("Semua Area") },
                        onClick = { state.selectedArea = null; state.areaMenuOpen = false }
                    )
                    areas.forEach { area ->
                        DropdownMenuItem(
                            text    = { Text(area) },
                            onClick = {
                                state.selectedArea = area
                                state.areaMenuOpen = false
                                // Reset table selection if it's in a different area
                                if (selectedTable != null && selectedTable.area != area) {
                                    state.tableUuid = null
                                }
                            }
                        )
                    }
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded         = state.tableMenuOpen,
            onExpandedChange = { state.tableMenuOpen = !state.tableMenuOpen }
        ) {
            OutlinedTextField(
                value         = selectedTable
                    ?.let { "${it.name}${it.area?.let { a -> " · $a" } ?: ""}" }
                    ?: "— Tanpa meja —",
                onValueChange = {},
                readOnly      = true,
                label         = { Text("Pilih Meja (opsional)") },
                leadingIcon   = { Icon(Icons.Default.TableBar, null, modifier = Modifier.size(18.dp)) },
                trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.tableMenuOpen) },
                modifier      = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded         = state.tableMenuOpen,
                onDismissRequest = { state.tableMenuOpen = false }
            ) {
                DropdownMenuItem(
                    text    = { Text("— Tanpa meja —") },
                    onClick = { state.tableUuid = null; state.tableMenuOpen = false }
                )
                activeTables.forEach { t ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(t.name)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment     = Alignment.CenterVertically
                                ) {
                                    if (!t.area.isNullOrBlank()) {
                                        Text(
                                            t.area,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            "·",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        "${t.capacity} kursi",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        onClick = { state.tableUuid = t.uuid; state.tableMenuOpen = false }
                    )
                }
            }
        }

        OutlinedTextField(
            value         = state.note,
            onValueChange = { state.note = it },
            label         = { Text("Catatan") },
            placeholder   = { Text("Preferensi tempat duduk, alergi, dll.") },
            maxLines      = 3,
            leadingIcon   = { Icon(Icons.Default.Notes, null, modifier = Modifier.size(18.dp)) },
            modifier      = Modifier.fillMaxWidth()
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// State holder
// ─────────────────────────────────────────────────────────────────────────────

class ReservationFormMutableState(editing: Reservation?) {
    var customerName  by mutableStateOf(editing?.customerName ?: "")
    var customerPhone by mutableStateOf(editing?.customerPhone ?: "")
    var partySizeStr  by mutableStateOf((editing?.partySize ?: 2).toString())
    var reservedDate  by mutableStateOf(editing?.reservedAt?.substringBefore('T') ?: "")
    var reservedTime  by mutableStateOf(editing?.reservedAt?.substringAfter('T', "")?.take(5) ?: "")
    var durationStr   by mutableStateOf((editing?.durationMinutes ?: 90).toString())
    var note          by mutableStateOf(editing?.note ?: "")
    var tableUuid: String? by mutableStateOf(editing?.tableUuid)
    var tableMenuOpen  by mutableStateOf(false)
    var areaMenuOpen   by mutableStateOf(false)
    var selectedArea: String? by mutableStateOf(null)

    fun canConfirm(notSubmitting: Boolean) = notSubmitting &&
        customerName.isNotBlank() &&
        (partySizeStr.toIntOrNull() ?: 0) >= 1 &&
        reservedDate.isNotBlank() &&
        reservedTime.isNotBlank() &&
        (durationStr.toIntOrNull() ?: 0) >= 1

    fun toInput() = ReservationInput(
        customerName    = customerName.trim(),
        customerPhone   = customerPhone.ifBlank { null }?.trim(),
        partySize       = partySizeStr.toIntOrNull() ?: 1,
        reservedAt      = "${reservedDate.trim()}T${reservedTime.trim()}",
        durationMinutes = durationStr.toIntOrNull() ?: 90,
        tableUuid       = tableUuid,
        note            = note.ifBlank { null }?.trim()
    )
}

@Composable
private fun rememberReservationFormState(editing: Reservation?) =
    remember(editing) { ReservationFormMutableState(editing) }

// ─────────────────────────────────────────────────────────────────────────────
// Section label
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(title: String, icon: ImageVector) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier              = Modifier.padding(top = 4.dp)
    ) {
        Icon(
            icon, contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint     = MaterialTheme.colorScheme.primary
        )
        Text(
            title,
            style      = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.primary
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.primaryContainer)
}
