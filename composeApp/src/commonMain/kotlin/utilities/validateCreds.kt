package utilities

// Username Rules: At least 5 characters excluding special characters
fun validateUsername(username: String): Boolean { // returns true if valid, false if not
    if (username.isBlank()) return false
    return (username.length >= 5 && username.matches(Regex("^[A-Za-z0-9_]*$")))
}

// Password Rules: 8 to 20 characters, no spaces
fun validatePassword(password: String): Boolean { // returns true if valid, false if not
    if (password.isBlank()) return false
    return (password.length in 8..20 && !password.contains(" "))
}


// I figure a more indepth explination was needed for this regex
/**
 * Validates if the provided email string adheres to the standard email format.
 *
 * - (?=.{1,64}@.{1,255}$) ensures:
     - The local part (before @) is at most 64 characters.
     - The domain part (after @) is at most 255 characters.

 * ([a-zA-Z0-9!#\$%&'*+/=?^_{|}~-]+(?:\\.[a-zA-Z0-9!#\$%&'*+/=?^_{|}~-]+)*):
     - Matches the local part of the email.
     - Allows valid characters like letters, digits, and special characters (!#$%&'*+/=?^_{|}~-`).
     - Allows dots (.) as long as they are not at the start, end, or consecutive.

 * - ([a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z]{2,}):
     - Matches the domain part of the email.
     - Allows subdomains and top-level domains (TLDs).
     - Ensures the TLD is at least 2 characters long (e.g., .com, .org).
 */

fun validateEmail(email: String) : Boolean = email.matches(
    Regex(
        "^(?=.{1,64}@.{1,255}$)([a-zA-Z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#\$%&'*+/=?^_`{|}~-]+)*)@([a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z]{2,})$"
    )
)