// This will be the main homepage/dashboard that a user will see upon logging in
package screens
import components.Report
import httpRequests.getReportByGroup
import httpRequests.postReport
import httpRequests.deleteReport
import httpRequests.fetchFile
import httpRequests.fetchFileNames
import utilities.*

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.*
import androidx.compose.material.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.ui.draw.clip

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

import io.ktor.client.*
import kotlinx.coroutines.launch
import java.io.File

enum class TabType {
    Local,
    State,
    National
}

@RequiresApi(Build.VERSION_CODES.O)
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
    onAddReport: () -> Unit,
    user : String?,
    client: HttpClient
) {
    val coroutineScope = rememberCoroutineScope() // Fixed typo
    var content by remember { mutableStateOf("") }
    var selectedFiles by remember { mutableStateOf<List<File>>(emptyList()) }

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

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
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
                                // Create a new report with media attachments
                                val newReport = Report(
                                    author = user.toString(),
                                    content = content,
                                    groupName = "local",
                                    // created date is autmomatically set by the server when the report is created
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
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

@Composable
fun FilePicker(
    onFilesSelected: (List<File>) -> Unit,
    context: Context = LocalContext.current
) {
    var selectedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { uris ->
            selectedUris = uris
            coroutineScope.launch {
                val files = uris.mapNotNull { uri ->
                    val file = uriToFile(context, uri) // Convert Uri to File
                    if (file != null && isSupportedFileType(file)) {
                        file // Only add supported files
                    } else {
                        // Message for user telling them which file is not supported
                        Toast.makeText(context, "Unsupported file type: ${file?.name}", Toast.LENGTH_SHORT).show()
                        null // Filter out unsupported files
                    }
                }
                onFilesSelected(files)
            }
        }
    )

    Column {
        // Trigger the file picker
        Button(onClick = {
            launcher.launch(arrayOf("image/*", "video/*")) // Only allow images and videos to be selected
        }) {
            Text("Select Files")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Display selected files
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(selectedUris) { uri ->
                ImagePreview(uri = uri, context = context)
            }
        }
    }
}

// Composable function to preview the images and videos selected by the user
@Composable
fun ImagePreview(uri: Uri, context: Context) {
    val imageBitmap by produceState<ImageBitmap?>(initialValue = null, uri) {
        value = loadImageBitmapFromUri(context, uri) // Call the suspend function
    }

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap!!,
            contentDescription = "Selected Image",
            modifier = Modifier
                .size(100.dp) // Set the size of the preview
                .clip(MaterialTheme.shapes.small) // Rounded
                .border(1.dp, Color.Gray) // Add
        )
    } else {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color.LightGray)
                .border(1.dp, Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Text("Preview Unavailable", style = MaterialTheme.typography.caption)
        }
    }
}

@Composable // Display saved reports for the selected tab
fun LocalTabContent(client: HttpClient, localReports: List<Report>, user: String?, onDeleteReport: (Report) -> Unit){
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(localReports) { report ->
            Reports(client, report, user, onDeleteReport)
        }
    }
}

@Composable
fun StateTabContent(client: HttpClient, stateReports: List<Report>, user: String?, onDeleteReport: (Report) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(stateReports) { report ->
            Reports(client, report, user, onDeleteReport)
        }
    }
}

@Composable
fun NationalTabContent(client: HttpClient, nationalReports: List<Report>, user: String?, onDeleteReport: (Report) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(nationalReports) { report ->
            Reports(client, report, user, onDeleteReport)
        }
    }
}

@Composable
fun Reports(client: HttpClient, report: Report, user: String?, onDeleteReport: (Report) -> Unit) {
    var showDialog by remember { mutableStateOf(false) } // State to control dialog visibility
    var fileNames by remember { mutableStateOf<List<String>>(emptyList()) } // List of file names
    val coroutineScope = rememberCoroutineScope()

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
            Text(text = report.createdDate.toString(), style = MaterialTheme.typography.caption)

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



@Composable
fun ImageAttachment(client: HttpClient, reportId: Long, fileName: String) {
    val coroutineScope = rememberCoroutineScope()
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(fileName) {
        coroutineScope.launch {
            try {
                val fileData = fetchFile(client, reportId, fileName)
                imageBitmap = BitmapFactory.decodeByteArray(fileData, 0, fileData.size)?.asImageBitmap()
            } catch (e: Exception) {
                println("Error loading image: ${e.message}")
            }
        }
    }

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap!!,
            contentDescription = "Image Attachment",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(MaterialTheme.shapes.medium)
                .border(1.dp, Color.Gray)
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.LightGray)
                .border(1.dp, Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Text("Image Unavailable", style = MaterialTheme.typography.caption)
        }
    }
}



@Composable
fun VideoAttachment(client: HttpClient, reportId: Long, fileName: String) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var videoUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(fileName) {
        coroutineScope.launch {
            try {
                // videos need to have avalid URI so they need a temp file to be used as a placeholder
                val fileData = fetchFile(client, reportId, fileName)
                val tempFile = File(context.cacheDir, fileName)
                tempFile.writeBytes(fileData)
                videoUri = Uri.fromFile(tempFile)
            } catch (e: Exception) {
                println("Error loading video: ${e.message}")
            }
        }
    }

    if (videoUri != null) {
        val exoPlayer = remember {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(androidx.media3.common.MediaItem.fromUri(videoUri!!))
                prepare()
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                exoPlayer.release() // Release the player when the composable is disposed
            }
        }

        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text("Video Unavailable", style = MaterialTheme.typography.caption)
        }
    }
}
