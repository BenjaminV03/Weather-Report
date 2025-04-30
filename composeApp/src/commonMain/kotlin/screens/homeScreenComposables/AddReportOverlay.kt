package screens.homeScreenComposables

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.ktor.client.*
import io.ktor.http.*
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.io.File
import java.util.*

@Composable
fun AddReportOverlay(
    onDismiss: () -> Unit,
    onAddReport: () -> Unit,
    user : String?,
    userLocation: Pair<Double, Double>,
    stateNames: List<String>,
    userState: String?,
    selectedTab: String,
    client: HttpClient
) {
    val coroutineScope = rememberCoroutineScope() // Fixed typo
    var content by remember { mutableStateOf("") }
    var selectedFiles by remember { mutableStateOf<List<File>>(emptyList()) }
    var loading by remember { mutableStateOf(false) } // post is being sent
    val characterLimit = 500 // Maximum character limit for content
    val context = LocalContext.current

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
                            loading = true
                            if (content.isNotBlank()) {

                                val groupName = when (selectedTab) {
                                    "Local" -> "local"
                                    "State" -> {
                                        if (userState != null && userState.lowercase(Locale.ROOT) in stateNames) {
                                            userState.lowercase(Locale.ROOT)
                                        } else {
                                            "unknown_state"
                                        }
                                    }
                                    "National" -> "national"
                                    else -> "unknown"
                                }
                                groupName.lowercase(Locale.getDefault())
                                if (groupName == "unknown" || groupName == "unknown_state") {
                                    println("Unable to determine groupname")
                                    Toast.makeText(context, "Unable to determine group name: $groupName", Toast.LENGTH_SHORT).show()
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
                                            withTimeout(60_000) { // Set a timeout of 1 minute
                                                postReport(client, newReport, selectedFiles).let { response ->
                                                    val message = when (response) {
                                                        HttpStatusCode.OK -> {
                                                            loading = false
                                                            onAddReport()
                                                            "Post Successful"
                                                        }
                                                        HttpStatusCode.BadRequest -> "Post Failed: Bad Request"
                                                        HttpStatusCode.PayloadTooLarge -> "Post Failed: File Size Too Large - Max 15MB"
                                                        HttpStatusCode.InternalServerError -> "Post Failed: File Size Too Large - Max 15MB"
                                                        else -> "Post Failed: Unexpected Error"
                                                    }
                                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        } catch (e: TimeoutCancellationException) {
                                            Toast.makeText(context, "Upload timed out. Please try again.", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
                                        } finally {
                                            loading = false
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Content cannot be empty", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = content.isNotBlank() && !loading // there needs to be content and no post is being sent
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text("Add")
                        }
                    }
                }
            }
        }
    }
}
