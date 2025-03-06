package org.example.project

import androidx.compose.runtime.*

import screens.*
import utilities.*
import components.classes.*
import components.FileReport.FileReportRepository
import co.touchlab.kermit.Logger

enum class Screen {
    CREATION, LOGIN, HOMEPAGE
}

@Composable
fun App() {
    var currentScreen: Screen by remember { mutableStateOf(Screen.CREATION) }
    var user: String? by remember { mutableStateOf(null) }
    val FRRepository = FileReportRepository()
    val reportList = FRRepository.getReports("local")
    val logger = Logger.withTag("App")

    logger.i {"Composite App Lauched"}
    when (currentScreen) {
        Screen.CREATION -> AccountCreationScreen(
            onRegister = { username: String, email: String, password: String ->
                // this is for testing purposes only
                // passwords will not be printed out
                println("Created user: Username=$username, Email=$email, Password=$password")
                user = username
                currentScreen = Screen.HOMEPAGE
            },
            onSwitchToLogin = { currentScreen = Screen.LOGIN }
        )
        Screen.LOGIN -> LoginScreen(
            onLogin = { identifier: String, password: Any? ->
                // once again, testing purposes only
                // passwords will not be printed out
                if (isEmail(identifier)) {
                    println("Logged in with an email: Email=$identifier, Password=$password")
                } else {
                    println("Logged in with a username: Username=$identifier, Password=$password")
                }
                user = identifier
                currentScreen = Screen.HOMEPAGE
            },
            onSwitchToRegister = { currentScreen = Screen.CREATION }
        )
        Screen.HOMEPAGE -> HomeScreen(
            user = user,
            onLogout = {
                println("Logged out")
                user = null
                currentScreen = Screen.LOGIN
            },
            reports = reportList

        )
    }
}
