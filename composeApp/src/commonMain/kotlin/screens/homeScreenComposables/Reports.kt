package screens.homeScreenComposables

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import components.Report
import httpRequests.fetchFileNames
import httpRequests.getLocationFromCoordinatesNominatim
import httpRequests.updateReport
import io.ktor.client.*
import kotlinx.coroutines.launch
import screens.homeScreenComposables.components.ImageAttachment
import screens.homeScreenComposables.components.VideoAttachment
import screens.homeScreenComposables.utilities.*

@Composable
fun Reports(
    client: HttpClient,
    report: Report,
    user: String?,
    onDeleteReport: (Report) -> Unit
){
    var showDialog by remember { mutableStateOf(false) } // State to control dialog visibility
    var showMenu by remember { mutableStateOf(false) } // State to control menu visibility
    var showEditOverlay by remember { mutableStateOf(false) } // State to control edit overlay visibility
    var fileNames by remember { mutableStateOf<List<String>>(emptyList()) } // List of file names
    val coroutineScope = rememberCoroutineScope()
    val createdDateTimeISO = parseIsoDate(report.createdDate)
    val createdDateTime = formatDateTime(createdDateTimeISO)
    var location by remember { mutableStateOf("") }
    coroutineScope.launch {
        location = getLocationFromCoordinatesNominatim(client, report.reportLat, report.reportLon)
    }



    // Check if the report is editable (within 5 minutes of creation)
    val isEditable = remember(report.createdDate) {
        isWithinLastMinutes(createdDateTimeISO, 5)
    }


    // Fetch file names for the report
    LaunchedEffect(report.id) {
        coroutineScope.launch {
            try {
                // Fetch file names from the server (you'll need an endpoint for this)
                fileNames = fetchFileNames(client, report.id!!)

            } catch (e: Exception) {
                println("Error fetching file names: ${e.message}")
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Display the author and content of the report
            Text(text = report.author, style = MaterialTheme.typography.caption)
            Spacer(modifier = Modifier.height(8.dp))

            Text(text = report.content, style = MaterialTheme.typography.body1)
            Spacer(modifier = Modifier.height(8.dp))

            // Display associated files
            fileNames.forEach { fileName ->
                when {
                    isImageFile(fileName) -> {
                        ImageAttachment(client, report.id!!, fileName)
                    }
                    isVideoFile(fileName) -> {
                        VideoAttachment(client, report.id!!, fileName)
                    }
                    else -> {
                        Text(
                            text = "Unsupported file: $fileName",
                            style = MaterialTheme.typography.body2,
                            color = Color.Red
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = location, style = MaterialTheme.typography.caption)
            // Display the created date
            if (createdDateTime != null) {
                Text(text = createdDateTime, style = MaterialTheme.typography.caption)
            }

            // Show menu icon if the report belongs to the current user
            if (report.author == user) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            if (isEditable) {
                                DropdownMenuItem(onClick = {
                                    // Handle edit action
                                    showMenu = false
                                    println("Edit action triggered for report: ${report.id}")
                                    showEditOverlay = true
                                }) {
                                    Text("Edit")
                                }
                            }
                            DropdownMenuItem(onClick = {
                                showMenu = false
                                showDialog = true // Show delete confirmation dialog
                            }) {
                                Text("Delete")
                            }
                        }
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


    // Edit overlay
    if (showEditOverlay) {
        EditReportOverlay(
            report = report,
            onDismiss = { showEditOverlay = false },
            onEditReport = { updatedReport ->
                // Handle the updated report
                coroutineScope.launch {
                    try {
                        updateReport(client, updatedReport)
                        showEditOverlay = false
                    } catch (e: Exception) {
                        println("Error updating report: ${e.message}")
                    }
                }
            },
        )
    }
}