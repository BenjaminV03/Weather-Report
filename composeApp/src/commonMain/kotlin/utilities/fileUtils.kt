package utilities

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


val supportedImageTypes = listOf("jpg", "jpeg", "png")
val supportedVideoTypes = listOf("mp4", "mkv")
val supportedFileTypes = supportedImageTypes + supportedVideoTypes


// Helper function to convert Uri to File
suspend fun uriToFile(context: Context, uri: Uri): File? = withContext(Dispatchers.IO) {
    val fileName = getFileName(context, uri) ?: return@withContext null
    val tempFile = File(context.cacheDir, fileName)

    try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        inputStream?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return@withContext null
    }

    return@withContext tempFile
}

// Helper function to get the file name from the Uri
fun getFileName(context: Context, uri: Uri): String? {
    var fileName: String? = null
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                fileName = it.getString(nameIndex)
            }
        }
    }
    if (fileName == null) {
        fileName = uri.path?.substringAfterLast('/')
    }
    return fileName
}

// Helper function to load an image bitmap from a Uri
suspend fun loadImageBitmapFromUri(context: Context, uri: Uri): ImageBitmap? = withContext(Dispatchers.IO) {
    return@withContext try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        bitmap?.asImageBitmap()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Helper function to load a video thumbnail from a Uri
fun loadVideoThumbnailFromUri(context: Context, uri: Uri): ImageBitmap? {
    return try {
        val retriever = android.media.MediaMetadataRetriever()
        retriever.setDataSource(context, uri)
        val bitmap = retriever.frameAtTime // Get the first frame as a thumbnail
        retriever.release()
        bitmap?.asImageBitmap()
    } catch (e: Exception) {
        println("Error loading video thumbnail: ${e.message}")
        null
    }
}



// Helper function to check if a file type is supported
fun isSupportedFileType(file: File): Boolean {
    val extension = file.extension.lowercase()
    return extension in supportedFileTypes
}

// Check if file is an image
fun isImageFile(file: String): Boolean {
    val extension = file.substringAfterLast('.', "").lowercase()
    return extension in supportedImageTypes
}
// Check if file is a video
fun isVideoFile(file: String): Boolean {
    val extension = file.substringAfterLast('.', "").lowercase()
    return extension in supportedVideoTypes
}


fun File.getMimeType(): String {
    val extension = this.extension.lowercase()
    return when (extension) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "mp4" -> "video/mp4"
        "mkv" -> "video/x-matroska"
        else -> "application/octet-stream" // Default MIME type for unknown files
    }
}
