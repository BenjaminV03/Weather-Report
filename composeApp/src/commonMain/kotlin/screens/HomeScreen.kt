// This will be the main homepage/dashboard that a user will see upon logging in
package screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import screens.homeScreenComposables.*
import httpRequests.getReportByGroup
import httpRequests.deleteReport
import components.Report
import components.User
import httpRequests.findUserByUsername
import kotlinx.coroutines.launch
import io.ktor.client.HttpClient
import screens.homeScreenComposables.BottomNavigationBar


enum class TabType {
    Local,
    State,
    National
}


val imageCache = mutableStateMapOf<String, ImageBitmap>() // Global cache for images
val videoCache = mutableStateMapOf<String, Uri>() // Global cache for videos


@Composable
fun HomeScreen(
    user: String?,
    onLogout: () -> Unit,
    client: HttpClient
) {
    var reports by remember { mutableStateOf(emptyList<Report>()) }
    var selectedTab by remember { mutableStateOf(TabType.Local) }
    val cachedReports = remember { mutableStateMapOf<TabType, List<Report>>() }

    var isOverlayVisible by remember { mutableStateOf(false) } // State to control overlay visibility
    var isInfoTabVisible by remember { mutableStateOf(false) } // State to control info tab visibility
    val coroutineScope = rememberCoroutineScope()
    var userInfo by remember { mutableStateOf<User?>(null) }

    // Function to refresh reports for the selected tab
    val refreshReports: () -> Unit = {
        coroutineScope.launch {
            try {
                val refreshedReports = when (selectedTab) {
                    TabType.Local -> getReportByGroup(client, "local")
                    TabType.State -> getReportByGroup(client, "state")
                    TabType.National -> getReportByGroup(client, "national")
                }
                cachedReports[selectedTab] = refreshedReports
                reports = refreshedReports
            } catch (e: Exception) {
                println("Error refreshing reports: ${e.message}")
            }
        }
    }

    // Function to delete a report
    val onDeleteReport: (Report) -> Unit = { reportToDelete ->
        coroutineScope.launch {
            try {
                // Call a suspend function to delete the report on the server
                deleteReport(client, reportToDelete.id)
                refreshReports() // Refresh reports after deletion
            } catch (e: Exception) {
                println("Error deleting report: ${e.message}")
            }
        }
    }


    LaunchedEffect(isInfoTabVisible) {
        if (user != null) {
            try {
                userInfo = findUserByUsername(client, user)
            } catch (e: Exception) {
                println("Error fetching user info: ${e.message}")
            }
        }
    }

    LaunchedEffect(selectedTab) {
        // Cache reports for each tab to allow for faster switching between tabs
        if (!cachedReports.containsKey(selectedTab)) { // Fixed null check
            try {
                val fetchedReports = when (selectedTab) {
                    TabType.Local -> getReportByGroup(client, "local")
                    TabType.State -> getReportByGroup(client, "state")
                    TabType.National -> getReportByGroup(client, "national")
                }
                cachedReports[selectedTab] = fetchedReports
            } catch (e: Exception) {
                println("Error fetching reports: ${e.message}")
            }
        }
        reports = cachedReports[selectedTab] ?: emptyList()
    }

    MaterialTheme(
        colors = MaterialTheme.colors.copy(
            primary = Color(0xFF1da60a),
            onPrimary = Color.White,
            background = Color.LightGray
        )
    ) {
        Box(modifier = Modifier.fillMaxSize())
        {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Weather Report") },
                        actions = {
                            IconButton(onClick = refreshReports) { // Add a refresh button
                                Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                            }
                            IconButton(onClick = { isInfoTabVisible = !isInfoTabVisible }) {
                                Icon(Icons.Default.Settings, contentDescription = "User Information")
                            }
                        }
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            isOverlayVisible = true // Show the overlay when FAB is clicked
                        }
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = "Add Report")
                    }
                },
                floatingActionButtonPosition = FabPosition.End,
                bottomBar = {
                    BottomAppBar {
                        BottomNavigationBar(selectedTab, onTabSelected = { selectedTab = it })
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (selectedTab) {
                        // Returns an empty list if the corresponding cachedReports is null just in case
                        TabType.Local -> LocalTabContent(client, cachedReports[selectedTab] ?: emptyList(), user, onDeleteReport)
                        TabType.State -> StateTabContent(client, cachedReports[selectedTab] ?: emptyList(), user, onDeleteReport)
                        TabType.National -> NationalTabContent(client, cachedReports[selectedTab] ?: emptyList(), user, onDeleteReport)
                    }
                }
            }

            // Overlay for adding a report
            if (isOverlayVisible) {
                AddReportOverlay(
                    onDismiss = { isOverlayVisible = false }, // Hide overlay when dismissed
                    onAddReport = {
                        // refresh new reports after adding
                        refreshReports()
                        isOverlayVisible = false // Hide overlay after adding
                    },
                    user = user,
                    client = client
                )
            }

            // Popout tab for user information
            if (isInfoTabVisible) {
                userInfo?.let {
                    UserInfoTab(
                        user = it,
                        onLogout = onLogout,
                        onClose = { isInfoTabVisible = false },
                        client = client // Pass the HttpClient for API calls
                    )
                }
            }
        }
    }
}

