package screens
import httpRequests.loginUser
import httpRequests.findUserByEmail
import utilities.isEmail
import components.User

import androidx.compose.runtime.*
import androidx.compose.material.*
import androidx.compose.foundation.layout.Column
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import io.ktor.client.*
import io.ktor.http.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import components.FormField

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
                when (response) {
                    HttpStatusCode.OK -> {
                        if (!isEmail(identifier)) {
                            onLogin(identifier) // its a username
                        } else { // its an email
                            val user = findUserByEmail(client, identifier)
                            onLogin(user.name)
                        }

                    }
                    HttpStatusCode.Forbidden -> {
                        errorMessage = "Invalid username or password"
                    }
                    else -> {
                        errorMessage = "Failed to login"
                    }
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

