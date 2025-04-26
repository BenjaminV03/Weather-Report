package screens

import utilities.*

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import httpRequests.registerUser
import io.ktor.client.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    fun validateInput(): Boolean {
        // Username Rules: At least 5 characters excluding special characters
        if (!validateUsername(username)) {
            message = "Username must be atleast 5 characters long and contain only letters, numbers, and underscores"
            return false
        }

        // More password rules will be added
        // Password Rules: 8-20 characters excluding spaces
        if (!validatePassword(password)) {
            message = "Password must be 8-20 characters long"
            return false
        }

        // Check if the email is valid
        if (!validateEmail(email)) {
            message = "Invalid email address"
            return false
        }

        return true
    }

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
                    if (!validateInput()) return@withContext // Validation failed
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