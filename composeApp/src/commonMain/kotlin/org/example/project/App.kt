package org.example.project

import androidx.compose.runtime.*
import androidx.compose.material.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.russhwolf.settings.Settings
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.utils.io.errors.*

import screens.*
import components.*
import httpRequests.*

enum class Screen {
    CREATION, LOGIN, HOMEPAGE
}

@Composable
fun App() {
    var currentScreen: Screen by remember { mutableStateOf(Screen.CREATION) }
    var user: String? by remember { mutableStateOf(null) }
    var reportList by remember { mutableStateOf<List<Report>>(emptyList()) }
    var isDataFetched by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val settings = Settings()
    val client = getClient() // from httpRequests
    val coroutineScope = rememberCoroutineScope()

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
            onSwitchToRegister = { currentScreen = Screen.CREATION }
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
                    reports = reportList
                )
            }
        }
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


