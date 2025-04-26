package screens.homeScreenComposables

import components.Report
import httpRequests.postReport
import screens.homeScreenComposables.components.FilePicker
import screens.homeScreenComposables.utilities.isSupportedFileType

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.ktor.client.*
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun AddReportOverlay(
    onDismiss: () -> Unit,
    onAddReport: () -> Unit,
    user : String?,
    roles: List<String>?,
    userLocation: Pair<Double, Double>,
    selectedTab: String,
    client: HttpClient
) {
    val coroutineScope = rememberCoroutineScope() // Fixed typo
    var content by remember { mutableStateOf("") }
    var selectedFiles by remember { mutableStateOf<List<File>>(emptyList()) }
    val characterLimit = 500 // Maximum character limit for content

    // Semi-transparent background overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)) // Add a semi-transparent background
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

                OutlinedTextField(
                    value = content,
                    onValueChange = {
                        if (it.length <= characterLimit) {
                            content = it // Update content when text changes within the limit
                        }
                    },
                    label = { Text("Content") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )

                // Display character count
                Text(
                    text = "${content.length} / $characterLimit",
                    style = MaterialTheme.typography.caption,
                    color = if (content.length == characterLimit) Color.Red else Color.Gray,
                    modifier = Modifier.align(Alignment.End)
                )

                Spacer(modifier = Modifier.height(16.dp))

                FilePicker(onFilesSelected = { files ->
                    selectedFiles = files.filter { isSupportedFileType(it) } // Filter files here
                })

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

                                val groupName = when (selectedTab) {
                                    "local" -> "local"
                                    "state" -> {
                                        val stateRolePattern = Regex("STATE_(\\w+)")
                                        val stateRole = roles?.firstOrNull { it.matches(stateRolePattern) }
                                        stateRole?.let { stateRolePattern.find(it)?.groupValues?.get(1) } ?: "unknown_state"
                                    }
                                    "national" -> "national"
                                    else -> "unknown"
                                }
                                if (groupName == "unknown" || groupName == "unknown_state") {

                                    onAddReport() // exit the report screen
                                } else {

                                    // Create a new report with media attachments
                                    val newReport = Report(
                                        id = null, // Set the ID to null since the server will generate one
                                        author = user.toString(), // Set the author of the report
                                        content = content, // Set the content of the report
                                        groupName = groupName, // Set the group name of the report
                                        reportLat = userLocation.first, // Set the latitude of the report
                                        reportLon = userLocation.second, // Set the longitude of the report
                                        )

                                    coroutineScope.launch {
                                        try {
                                            postReport(client, newReport, selectedFiles)
                                            onAddReport()
                                        } catch (e: Exception) {
                                            println("Error adding report: ${e.message}")
                                        }
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
