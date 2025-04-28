package components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

// Composable for the homescreen to pop up a dialog when the user has their location off

@Composable
fun LocationPermissionDialog(
    onDismiss: () -> Unit,
    onEnableLocation: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Enable Location") },
        text = { Text("Location services are required to access this feature. Please enable location sharing.") },
        confirmButton = {
            Button(onClick = {
                onEnableLocation()
                onDismiss()
            }) {
                Text("Enable")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}
