package id.rancak.app.presentation.ui.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.presentation.designsystem.RancakTheme

/**
 * Panel editor informasi toko — nama, alamat, telepon, dan footer struk yang
 * akan dicetak di bagian atas/bawah tiap struk.
 */
@Composable
internal fun StoreContent(
    storeName: String,
    storeAddress: String,
    storePhone: String,
    footerText: String,
    merchantQrisString: String,
    onStoreName: (String) -> Unit,
    onStoreAddress: (String) -> Unit,
    onStorePhone: (String) -> Unit,
    onFooterText: (String) -> Unit,
    onMerchantQrisString: (String) -> Unit
) {
    SettingsCard {
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

        Spacer(Modifier.height(14.dp))
        HorizontalDivider()
        Spacer(Modifier.height(10.dp))

        Text(
            "QRIS Statis Merchant",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Tempel string EMVCo QRIS merchant Anda. Dipakai untuk split bill " +
            "QRIS \u2014 setiap pelanggan scan QR yang sama dan memasukkan " +
            "nominal sesuai bagiannya.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = merchantQrisString,
            onValueChange = onMerchantQrisString,
            label = { Text("QRIS String") },
            placeholder = { Text("00020101...") },
            leadingIcon = { Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(18.dp)) },
            minLines = 2,
            maxLines = 4,
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
                merchantQrisString   = "",
                onStoreName          = {},
                onStoreAddress       = {},
                onStorePhone         = {},
                onFooterText         = {},
                onMerchantQrisString = {}
            )
        }
    }
}
