package screens.homeScreenComposables.components

import screens.videoCache

import android.net.Uri
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import httpRequests.fetchFile
import io.ktor.client.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.*


@Composable
fun VideoAttachment(client: HttpClient, reportId: UUID, fileName: String) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var videoUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(fileName) {
        coroutineScope.launch {
            if (videoCache.containsKey(fileName)) {
                // Use cached video URI
                videoUri = videoCache[fileName]
            } else {
                try {
                    val fileData = fetchFile(client, reportId, fileName)
                    val tempFile = File(context.cacheDir, fileName)
                    tempFile.writeBytes(fileData)
                    val uri = Uri.fromFile(tempFile)
                    videoCache[fileName] = uri // Cache the video URI
                    videoUri = uri
                } catch (e: Exception) {
                    println("Error loading video: ${e.message}")
                }
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