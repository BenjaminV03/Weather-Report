package screens

import androidx.compose.runtime.*
import androidx.compose.material.*
import androidx.compose.foundation.layout.Column
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import com.russhwolf.settings.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import components.FormField
import kotlinx.coroutines.IO
import httpRequests.*

@Composable
fun LoginScreen(
    client: HttpClient,
    onLogin: (String) -> Unit,
    onSwitchToRegister: () -> Unit
) {
    // this can be username or email
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
            onValueChange = { password = it },
            label = "Password",
            isPassword = true
        )
        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            CoroutineScope(Dispatchers.IO).launch {
                val response = loginUser(client, identifier, password)
                if (response == HttpStatusCode.OK) {
                    onLogin(identifier)
                } else {
                    errorMessage = response.toString()
                }
            }
        }) {
            Text("Login")
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onSwitchToRegister) {
            Text("Don't have an account? Register")
        }

        errorMessage?.let {
            Spacer(Modifier.height(16.dp))
            Text(it, color = MaterialTheme.colors.error)
        }
    }
}

