// This utility checks if input is a valid email address
// If the check fails, it defaults to a username

package utilities

import androidx.*

fun isEmail(input: String): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    return Regex(emailRegex).matches(input)
}