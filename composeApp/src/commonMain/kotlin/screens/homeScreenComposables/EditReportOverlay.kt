package screens.homeScreenComposables

import components.Report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun EditReportOverlay(
    report: Report,
    onDismiss: () -> Unit,
    onEditReport: (Report) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var content by remember { mutableStateOf(report.content) }
    val characterLimit = 500 // Maximum character limit for content

    // Semi-transparent background overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
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
                Text("Edit Report", style = MaterialTheme.typography.h6)

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = {
                        if (it.length <= characterLimit) {
                            content = it
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
                                // Update the report with new content. Files cannot be updated.
                                val updatedReport = report.copy(
                                    content = content // Update the content
                                )

                                coroutineScope.launch {
                                    try {
                                        onEditReport(updatedReport)
                                    } catch (e: Exception) {
                                        println("Error updating report: ${e.message}")
                                    }
                                }
                            }
                        }
                    ) {
                        Text("Update")
                    }
                }
            }
        }
    }
}
