package screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.*
import androidx.compose.material.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import com.russhwolf.settings.Settings
import httpRequests.*

@Composable
fun AccountCreationScreen(
    client : HttpClient,
    onRegister: (String) -> Unit,
    onSwitchToLogin: () -> Unit,
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") }
        )
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )
        Button(onClick = {
            coroutineScope.launch {
                withContext(Dispatchers.Default) {
                    val response = registerUser(client, username, password, email)
                    if (response == HttpStatusCode.OK) {
                        onRegister(username)
                    } else {
                        message = "Registration failed"
                        println(response)
                    }
                }
            }
        }) {
            Text("Register")
        }
        TextButton(onClick = onSwitchToLogin) {
            Text("Already have an account? Login")
        }
        if (message.isNotEmpty()) {
            Snackbar {
                Text(message)
            }
        }
    }
}


data class RegisterRequest(val username: String, val password: String, val email: String)