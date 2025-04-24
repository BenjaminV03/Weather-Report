package screens.homeScreenComposables.components

import screens.imageCache

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import httpRequests.fetchFile
import io.ktor.client.*
import kotlinx.coroutines.launch
import java.util.*


@Composable
fun ImageAttachment(client: HttpClient, reportId: UUID, fileName: String) {
    val coroutineScope = rememberCoroutineScope()
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(fileName) {
        coroutineScope.launch {
            if (imageCache.containsKey(fileName)) {
                // Use cached image
                imageBitmap = imageCache[fileName]
            } else {
                try {
                    val fileData = fetchFile(client, reportId, fileName)
                    val bitmap = BitmapFactory.decodeByteArray(fileData, 0, fileData.size)?.asImageBitmap()
                    if (bitmap != null) {
                        imageCache[fileName] = bitmap // Cache the image
                        imageBitmap = bitmap
                    }
                } catch (e: Exception) {
                    println("Error loading image: ${e.message}")
                }
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