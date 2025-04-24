package screens.homeScreenComposables.components

import screens.homeScreenComposables.utilities.*

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.io.File


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
            if (uris.isEmpty()) return@rememberLauncherForActivityResult // Return if no files are selected

            selectedUris = selectedUris + uris.filter { it !in selectedUris } // Avoid duplicates

            coroutineScope.launch {
                val files = selectedUris.mapNotNull { uri ->
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
                val fileName = getFileName(context, uri) ?: "Unknown"
                when {
                    isVideoFile(fileName) -> VideoPreview(uri = uri, context = context)
                    isImageFile(fileName) -> ImagePreview(uri = uri, context = context)
                    else -> {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(Color.LightGray)
                                .border(1.dp, Color.Gray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Unsupported File", style = MaterialTheme.typography.caption, color = Color.Red)
                        }
                    }
                }
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


@Composable
fun VideoPreview(uri: Uri, context: Context) {
    val videoThumbnail by produceState<ImageBitmap?>(initialValue = null, uri) {
        value = loadVideoThumbnailFromUri(context, uri) // Call function to load thumbnail
    }

    if (videoThumbnail != null) {
        Image(
            bitmap = videoThumbnail!!,
            contentDescription = "Video Thumbnail",
            modifier = Modifier
                .size(100.dp) // Set the size of the preview
                .clip(MaterialTheme.shapes.small) // Rounded
                .border(1.dp, Color.Gray) // Add border
        )
    } else {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color.LightGray)
                .border(1.dp, Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Text("Thumbnail Unavailable", style = MaterialTheme.typography.caption)
        }
    }
}