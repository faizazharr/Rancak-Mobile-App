package id.rancak.app.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import id.rancak.app.presentation.designsystem.RancakTheme

@Composable
fun RancakButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(6.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun RancakOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        enabled = enabled,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

// ── Previews ──

@Preview
@Composable
private fun RancakButtonPreview() {
    RancakTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            RancakButton(text = "Bayar Sekarang", onClick = {}, modifier = Modifier.fillMaxWidth())
            RancakButton(text = "Loading...", onClick = {}, isLoading = true, modifier = Modifier.fillMaxWidth())
            RancakButton(text = "Disabled", onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Preview
@Composable
private fun RancakOutlinedButtonPreview() {
    RancakTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            RancakOutlinedButton(text = "Batal", onClick = {}, modifier = Modifier.fillMaxWidth())
            RancakOutlinedButton(text = "Disabled", onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth())
        }
    }
}
