package utilities

// uses Regexes to check if a string is an email
fun isEmail(email: String) : Boolean = email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"))