package id.rancak.app.presentation.ui.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.presentation.designsystem.RancakTheme

/**
 * Panel editor informasi toko dan kustomisasi tampilan struk.
 * Field yang disinkronkan ke `/receipt-settings` di-push ke server
 * setiap kali nilai berubah (fire-and-forget via ViewModel).
 */
@Composable
internal fun StoreContent(
    // Identitas toko
    storeName: String,
    storeAddress: String,
    storePhone: String,
    onStoreName: (String) -> Unit,
    onStoreAddress: (String) -> Unit,
    onStorePhone: (String) -> Unit,
    // Footer & header
    footerText: String,
    receiptHeader: String,
    receiptFooter2: String,
    onFooterText: (String) -> Unit,
    onReceiptHeader: (String) -> Unit,
    onReceiptFooter2: (String) -> Unit,
    // Logo
    showLogo: Boolean,
    onShowLogo: (Boolean) -> Unit,
    // Tampilan struk
    receiptNameSize: String,
    separatorStyle: String,
    footerPosition: String,
    onReceiptNameSize: (String) -> Unit,
    onSeparatorStyle: (String) -> Unit,
    onFooterPosition: (String) -> Unit,
    // Kontak tambahan
    receiptEmail: String,
    receiptWebsite: String,
    receiptNpwp: String,
    onReceiptEmail: (String) -> Unit,
    onReceiptWebsite: (String) -> Unit,
    onReceiptNpwp: (String) -> Unit,
    // Sosial media & WiFi
    receiptInstagram: String,
    receiptFacebook: String,
    receiptWifiSsid: String,
    receiptWifiPassword: String,
    onReceiptInstagram: (String) -> Unit,
    onReceiptFacebook: (String) -> Unit,
    onReceiptWifiSsid: (String) -> Unit,
    onReceiptWifiPassword: (String) -> Unit,
) {
    SettingsCard {
        // ── Identitas Toko ───────────────────────────────────────────────────
        Text(
            "Info ini tampil di header setiap struk yang dicetak.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = storeName, onValueChange = onStoreName,
            label = { Text("Nama Toko") },
            placeholder = { Text("Rancak Coffee") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Store, contentDescription = null, modifier = Modifier.size(18.dp)) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = storeAddress, onValueChange = onStoreAddress,
            label = { Text("Alamat") },
            placeholder = { Text("Jl. Sudirman No. 1") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp)) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = storePhone, onValueChange = onStorePhone,
                label = { Text("Telepon") },
                placeholder = { Text("0812-3456-7890") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodySmall
            )
            OutlinedTextField(
                value = footerText, onValueChange = onFooterText,
                label = { Text("Footer Struk") },
                placeholder = { Text("Terima kasih!") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.TextFields, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = receiptHeader, onValueChange = onReceiptHeader,
            label = { Text("Header Tambahan") },
            placeholder = { Text("Tagline atau ucapan selamat datang") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(18.dp)) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = receiptFooter2, onValueChange = onReceiptFooter2,
            label = { Text("Footer Baris 2") },
            placeholder = { Text("Kebijakan retur atau info promo") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.TextFields, contentDescription = null, modifier = Modifier.size(18.dp)) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodySmall
        )

        Spacer(Modifier.height(14.dp))
        HorizontalDivider()
        Spacer(Modifier.height(10.dp))

        // ── Logo Struk ───────────────────────────────────────────────────────
        Text(
            "Logo Struk",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    "Tampilkan logo di atas struk",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Switch(checked = showLogo, onCheckedChange = onShowLogo)
        }
        Text(
            "Logo toko ditampilkan di area paling atas struk, sebelum nama toko.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(14.dp))
        HorizontalDivider()
        Spacer(Modifier.height(10.dp))

        // ── Tampilan Struk ───────────────────────────────────────────────────
        Text(
            "Tampilan Struk",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))

        Text(
            "Ukuran Nama Toko",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("normal" to "Normal", "large" to "Besar", "xlarge" to "Ekstra").forEach { (value, label) ->
                FilterChip(
                    selected = receiptNameSize == value,
                    onClick = { onReceiptNameSize(value) },
                    label = {
                        Icon(Icons.Default.FormatSize, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(label, style = MaterialTheme.typography.labelSmall)
                    }
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            "Garis Pemisah",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("dashed" to "---", "double" to "===", "none" to "Tanpa").forEach { (value, label) ->
                FilterChip(
                    selected = separatorStyle == value,
                    onClick = { onSeparatorStyle(value) },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            "Posisi Footer",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("left" to "Kiri", "center" to "Tengah", "right" to "Kanan").forEach { (value, label) ->
                FilterChip(
                    selected = footerPosition == value,
                    onClick = { onFooterPosition(value) },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        HorizontalDivider()
        Spacer(Modifier.height(10.dp))

        // ── Kontak Tambahan ───────────────────────────────────────────────────
        Text(
            "Kontak Tambahan",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Email, website, dan NPWP akan dicetak jika diisi.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = receiptEmail, onValueChange = onReceiptEmail,
            label = { Text("Email") },
            placeholder = { Text("toko@email.com") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp)) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = receiptWebsite, onValueChange = onReceiptWebsite,
                label = { Text("Website") },
                placeholder = { Text("www.toko.com") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodySmall
            )
            OutlinedTextField(
                value = receiptNpwp, onValueChange = onReceiptNpwp,
                label = { Text("NPWP") },
                placeholder = { Text("00.000.000.0-000.000") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(14.dp))
        HorizontalDivider()
        Spacer(Modifier.height(10.dp))

        // ── Sosial Media & WiFi ───────────────────────────────────────────────
        Text(
            "Sosial Media & WiFi",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Akan dicetak di bagian bawah struk jika diisi.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = receiptInstagram, onValueChange = onReceiptInstagram,
                label = { Text("Instagram") },
                placeholder = { Text("namacafe (tanpa @)") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodySmall
            )
            OutlinedTextField(
                value = receiptFacebook, onValueChange = onReceiptFacebook,
                label = { Text("Facebook") },
                placeholder = { Text("nama atau URL") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = receiptWifiSsid, onValueChange = onReceiptWifiSsid,
            label = { Text("WiFi SSID") },
            placeholder = { Text("NamaJaringanWifi") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(18.dp)) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = receiptWifiPassword, onValueChange = onReceiptWifiPassword,
            label = { Text("Password WiFi") },
            placeholder = { Text("Kosongkan jika tidak ada password") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(18.dp)) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodySmall
        )
    }
}

@Preview
@Composable
private fun StoreContentPreview() {
    RancakTheme {
        Column(modifier = Modifier.padding(12.dp)) {
            StoreContent(
                storeName            = "Rancak Coffee",
                storeAddress         = "Jl. Sudirman No. 1",
                storePhone           = "0812-3456-7890",
                footerText           = "Terima kasih!",
                receiptHeader        = "",
                receiptFooter2       = "",
                showLogo             = false,
                receiptNameSize      = "large",
                separatorStyle       = "dashed",
                footerPosition       = "center",
                receiptEmail         = "",
                receiptWebsite       = "",
                receiptNpwp          = "",
                receiptInstagram     = "",
                receiptFacebook      = "",
                receiptWifiSsid      = "",
                receiptWifiPassword  = "",
                onStoreName          = {},
                onStoreAddress       = {},
                onStorePhone         = {},
                onFooterText         = {},
                onReceiptHeader      = {},
                onReceiptFooter2     = {},
                onShowLogo           = {},
                onReceiptNameSize    = {},
                onSeparatorStyle     = {},
                onFooterPosition     = {},
                onReceiptEmail       = {},
                onReceiptWebsite     = {},
                onReceiptNpwp        = {},
                onReceiptInstagram   = {},
                onReceiptFacebook    = {},
                onReceiptWifiSsid    = {},
                onReceiptWifiPassword = {}
            )
        }
    }
}
