package screens.homeScreenComposables.utilities

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


//Parses a date string in ISO format and returns a Date object.

fun parseIsoDate(dateString: String?): Date? {
    return try {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS", Locale.US)
        isoFormat.timeZone = TimeZone.getTimeZone("EST")
        isoFormat.parse(dateString.toString())
    } catch (e: Exception) {
        println("Error parsing date: ${e.message}")
        null
    }
}

// Formats a Date object into a readable string.
fun formatDateTime(date: Date?): String? {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    return date?.let { formatter.format(it) }
}

// Checks if a given date is within the last X minutes.
fun isWithinLastMinutes(date: Date?, minutes: Long): Boolean {
    val currentTime = System.currentTimeMillis()
    val duration = currentTime - (date?.time ?: 0)
    println("Current time: $currentTime")
    println("Duration: $duration")
    return duration <= minutes * 60 * 1000
}
