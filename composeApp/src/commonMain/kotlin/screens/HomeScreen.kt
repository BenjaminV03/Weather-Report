// This will be the main homepage/dashboard that a user will see upon logging in
package screens
import components.Report
import httpRequests.getReportByGroup
import httpRequests.postReport
import httpRequests.deleteReport

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.material.*
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.ktor.client.*
import kotlinx.coroutines.launch

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
    val coroutineScope = rememberCoroutineScope()

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

    LaunchedEffect(selectedTab) {
        // Cache reports for each tab to allow for faster switching between tabs
        if (cachedReports[selectedTab] == null) {
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
                        title = { Text("Weather Report - $user") },
                        actions = {
                            IconButton(onClick = refreshReports) { // Add a refresh button
                                Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                            }
                            IconButton(onClick = onLogout) {
                                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
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
                        TabType.Local -> LocalTabContent(cachedReports[selectedTab] ?: emptyList(), user, onDeleteReport)
                        TabType.State -> StateTabContent(cachedReports[selectedTab] ?: emptyList(), user, onDeleteReport)
                        TabType.National -> NationalTabContent(cachedReports[selectedTab] ?: emptyList(), user, onDeleteReport)
                    }
                }
            }

            // Overlay for adding a report
            if (isOverlayVisible) {
                AddReportOverlay(
                    onDismiss = { isOverlayVisible = false }, // Hide overlay when dismissed
                    onAddReport = { newReport ->
                        // Add the new report to the list
                        reports = reports + newReport
                        refreshReports()
                        isOverlayVisible = false // Hide overlay after adding
                    },
                    user = user,
                    client = client
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    selectedTab: TabType,
    onTabSelected: (TabType) -> Unit,
) {
    BottomNavigation {
        BottomNavigationItem(
            icon = { Icon(Icons.AutoMirrored.Default.List, contentDescription = "Reports") },
            label = { Text("Local") },
            selected = selectedTab == TabType.Local,
            onClick = { onTabSelected(TabType.Local) }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.AutoMirrored.Default.List, contentDescription = "Trending") },
            label = { Text("State") },
            selected = selectedTab == TabType.State,
            onClick = { onTabSelected(TabType.State) }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.AutoMirrored.Default.List, contentDescription = "Notifications") },
            label = { Text("National") },
            selected = selectedTab == TabType.National,
            onClick = { onTabSelected(TabType.National) }
        )
    }
}

@Composable
fun AddReportOverlay(
    onDismiss: () -> Unit,
    onAddReport: (Report) -> Unit,
    user : String?,
    client: HttpClient
) {
    val corutineScope = rememberCoroutineScope()
    // Semi-transparent background overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onDismiss) // Dismiss overlay when background is clicked
    ) {
        // Centered content
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .background(Color.White, shape = MaterialTheme.shapes.medium)
                .align(Alignment.Center)
                .padding(16.dp)
        ) {
            Column {
                Text("Add New Report", style = MaterialTheme.typography.h6)

                Spacer(modifier = Modifier.height(16.dp))

                var content by remember { mutableStateOf("") }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (content.isNotBlank()) {
                                val newReport = Report(
                                    author = user.toString(),
                                    content = content,
                                    groupName = "local"
                                )
                                corutineScope.launch {
                                    try {
                                        postReport(client, newReport)
                                        onAddReport(newReport)
                                    } catch (e: Exception) {
                                        println("Error adding report: ${e.message}")
                                    }
                                }
                            }
                        }
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}





enum class TabType {
    Local,
    State,
    National
}

@Composable
fun LocalTabContent(localReports: List<Report>, user: String?, onDeleteReport: (Report) -> Unit){
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(localReports) { report ->
            Reports(report, user, onDeleteReport)
        }
    }
}

@Composable
fun StateTabContent(stateReports: List<Report>, user: String?, onDeleteReport: (Report) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(stateReports) { report ->
            Reports(report, user, onDeleteReport)
        }
    }
}

@Composable
fun NationalTabContent(nationalReports: List<Report>, user: String?, onDeleteReport: (Report) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(nationalReports) { report ->
            Reports(report, user, onDeleteReport)
        }
    }
}



@Composable
fun Reports(report: Report, user: String?, onDeleteReport: (Report) -> Unit){
    var showDialog by remember { mutableStateOf(false) } // State to control dialog visibility

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = 4.dp
    ){
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = report.author, style = MaterialTheme.typography.caption)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = report.content, style = MaterialTheme.typography.body1)

            // Show delete button if the report belongs to the current user
            if (report.author == user) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { showDialog = true }, // Show dialog on button click
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray) // Less bright color
                    ) {
                        Text("Delete", color = Color.Black) // Adjust text color for contrast
                    }
                }
            }
        }
    }


    // Confirmation dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false }, // Close dialog on dismiss
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete this report?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        onDeleteReport(report) // Call delete function
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDialog = false } // Close dialog without deleting
                ) {
                    Text("No")
                }
            }
        )
    }
}