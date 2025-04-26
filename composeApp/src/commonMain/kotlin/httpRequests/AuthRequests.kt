package httpRequests
import utilities.validateEmail


import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import com.russhwolf.settings.Settings
import io.ktor.client.call.*
import kotlinx.serialization.Serializable

val authEndpoints = config.authEndpoints


// these functions are used to communicate auth requests to the spring backend

// login request
suspend fun loginUser(client: HttpClient, identifier: String, password: String): HttpStatusCode {
    println("Login request: $identifier")
    try {
        // Send the login request to the server
        val authRequest: AuthRequest = if (validateEmail(identifier)) { // email was entered
            AuthRequest(
                password = password,
                email = identifier
            )
        } else{ // username was entered
            AuthRequest(
                username = identifier,
                password = password
            )
        }
        val response: HttpResponse = client.post("$baseurl${authEndpoints.login}") {
            contentType(ContentType.Application.Json)
            setBody(authRequest)
        }
        if (response.status == HttpStatusCode.OK) {
            val authToken = response.bodyAsText() // Assuming the token is returned as plain text
            // Save the token securely (e.g., in shared preferences)
            Settings().putString("authToken", authToken)
            return response.status
        } else {
            println("Login failed: ${response.status.description}")
            return response.status // failed to login
        }
    } catch (e: Exception) {
        print("An error occurred: ${e.message}")
        return HttpStatusCode.BadRequest
    }
}

// register request
suspend fun registerUser(client: HttpClient, username: String, password: String, email: String): HttpStatusCode {
    val response: HttpResponse = client.post("$baseurl${authEndpoints.register}") {
        contentType(ContentType.Application.Json)
        setBody(AuthRequest(username, password, email))
    }
    if (response.status == HttpStatusCode.OK) {
        val authToken = response.bodyAsText()
        Settings().putString("authToken", authToken)
        print("Registration successful")
        return response.status // successful registration

    } else {
        return response.status // failed to register
    }
}

// validate token
suspend fun validateToken(client: HttpClient): Boolean {
    val token = Settings().getStringOrNull("authToken") ?: return false
    println("Validating token...")
    return try {
        val response: HttpResponse = client.post("$baseurl${authEndpoints.verifyToken}") {
            header("Authorization", "Bearer $token")
        }
        println("Token validation successful: ${response.status}")
        response.status == HttpStatusCode.OK
    } catch (e: Exception) {
        println("Token validation failed: ${e.message}")
        false
    }
}

// extract user information from token
suspend fun extractUserInfo(client: HttpClient): UserInfoResponse? {
    println("Extracting user info...")
    val token = Settings().getStringOrNull("authToken") ?: return null
    return try {
        val response: HttpResponse = client.post("$baseurl${authEndpoints.extractUserInfo}") {
            header("Authorization", "Bearer $token")
        }
        if (response.status == HttpStatusCode.OK) {
            // Parse the response body as a map
            val userInfo = response.body<UserInfoResponse>()
            userInfo
        } else {
            println("Failed to extract user info: ${response.status.description}")
            null
        }
    } catch (e: Exception) {
        println("Failed to extract user info: ${e.message}")
        null
    }
}





@Serializable
data class AuthRequest(
    val username: String? = null,
    val password: String,
    val email: String? = null
)

@Serializable
data class UserInfoResponse(
    val username: String,
    val roles: List<String>
)