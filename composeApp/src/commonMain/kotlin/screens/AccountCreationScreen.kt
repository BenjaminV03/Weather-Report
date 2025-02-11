package screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import components.FormField


@Composable
fun AccountCreationScreen(onRegister: (String, String, String) -> Unit, onSwitchToLogin: () -> Unit) {
    var username: String by remember { mutableStateOf("") }
    var email: String by remember { mutableStateOf("") }
    var password: String by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Create Account")

        Spacer(Modifier.height(16.dp))

        FormField(
            value = username, onValueChange = { username = it },
            label = "Username"
        )
        FormField(
            value = email, onValueChange = { email = it },
            label = "Email"
        )
        FormField(
            value = password, onValueChange = { password = it },
            label = "Password",
            isPassword = true
        )

        // there is currently no check on the password, will add later on

        Spacer(Modifier.height(16.dp))

        Button(onClick = { onRegister(username, email, password) }) {
            Text("Register")
        }

        Spacer(Modifier.height(8.dp))

        TextButton(onClick = onSwitchToLogin) {
            Text("Already have an account? Log in")
        }
    }
}

