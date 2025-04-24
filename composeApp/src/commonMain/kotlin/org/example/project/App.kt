package org.example.project

import androidx.compose.runtime.*
import androidx.compose.material.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.russhwolf.settings.Settings
import io.ktor.utils.io.errors.*

import screens.*
import components.*
import httpRequests.*

enum class Screen {
    CREATION, LOGIN, HOMEPAGE, DEBUG
}

@Composable
fun App() {
    var currentScreen: Screen by remember { mutableStateOf(Screen.LOGIN) }
    var user: String? by remember { mutableStateOf(null) }
    var reportList by remember { mutableStateOf<List<Report>>(emptyList()) }
    var isDataFetched by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val settings = Settings()
    val client = getClient()

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isLoading = true
        println("Starting application")
        val isValidToken = validateToken(client)
        if (isValidToken) {
            println("Valid token. Welcome Back")
            val userInfo = extractUserInfo(client)
            if (userInfo != null) {
                user = userInfo.username
                println(user)
                currentScreen = Screen.HOMEPAGE
            } else {
                println("Invalid or no token")
                settings.remove("authToken") // Clear invalid token
                currentScreen = Screen.LOGIN
            }

            isLoading = false
        } else {
            println("No valid token")
            currentScreen = Screen.LOGIN
            isLoading = false
        }
    }



    fun fetchData() {
        if (!isDataFetched) {
            coroutineScope.launch {
                try {
                    val fetchedReports = getAllReports(client)
                    withContext(Dispatchers.Main) {
                        reportList = fetchedReports
                        println("Fetched reports: $fetchedReports")
                        isDataFetched = true
                        errorMessage = null
                    }
                } catch (e: IOException) {
                    withContext(Dispatchers.Main) {
                        errorMessage = "Network error: ${e.message}"
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        errorMessage = "Error fetching reports: ${e.message}"
                    }
                }
            }
        }
    }

    if (isLoading) {
        // show loading screen
        LoadingScreen()
        return
    }


    when (currentScreen) {
        Screen.CREATION -> AccountCreationScreen(
            client = client,
            onRegister = {username: String ->
                user = username
                currentScreen = Screen.HOMEPAGE
            },
            onSwitchToLogin = { currentScreen = Screen.LOGIN },
        )
        Screen.LOGIN -> LoginScreen(
            client = client,
            onLogin = {identifier: String ->
                user = identifier
                currentScreen = Screen.HOMEPAGE
            },
            onSwitchToRegister = { currentScreen = Screen.CREATION },
            onSwitchToDebug = { currentScreen = Screen.DEBUG }
        )
        Screen.HOMEPAGE -> {
            if (!isDataFetched) {
                fetchData()
            }
            if (errorMessage != null) {
                Text(errorMessage!!)
            } else if (!isDataFetched) {
                LoadingScreen()
            } else {
                HomeScreen(
                    user = user,
                    onLogout = {
                        println("Logged out")
                        user = null
                        settings.remove("authToken")
                        reportList = emptyList()
                        isDataFetched = false
                        currentScreen = Screen.LOGIN
                    },
                    client = client
                )
            }
        }
        Screen.DEBUG -> DebugScreen(
            currentBaseUrl = baseurl,
            onBaseUrlChange = { newBaseUrl ->
                changeBaseUrl(newBaseUrl)
            },
            onBack = { currentScreen = Screen.LOGIN }
        )
    }
}


@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}


@Composable
fun DebugScreen(
    currentBaseUrl: String,
    onBaseUrlChange: (String) -> Unit,
    onBack: () -> Unit
) {
    var newBaseUrl by remember { mutableStateOf(currentBaseUrl) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Debug Screen", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Current Base URL: $currentBaseUrl")
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = newBaseUrl,
            onValueChange = { newBaseUrl = it },
            label = { Text("New Base URL") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onBaseUrlChange(newBaseUrl) }) {
            Text("Update Base URL")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack) {
            Text("Back")
        }
    }
}
