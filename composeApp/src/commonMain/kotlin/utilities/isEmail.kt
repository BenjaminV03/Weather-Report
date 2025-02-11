// This utility checks if input is a valid email address
// If the check fails, it defaults to a username

package utilities

fun isEmail(input: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches()
}