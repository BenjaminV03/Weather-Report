package screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import components.FormField

@Composable
fun LoginScreen(onLogin: (String, Any) -> Unit, onSwitchToRegister: () -> Unit) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FormField(
            value = identifier,
            onValueChange = { identifier = it },
            label = "Username or Email"
        )
        FormField(
            value = password,
            onValueChange = {password = it},
            label = "Password",
            isPassword = true
        )
        Spacer(Modifier.height(16.dp))

        Button(onClick = { onLogin(identifier, password) }) {
            Text("Login")
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onSwitchToRegister) {
            Text("Don't have an account? Register")
        }
    }
}