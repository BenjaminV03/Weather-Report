package httpRequests
import utilities.isEmail


import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import com.russhwolf.settings.Settings
import kotlinx.serialization.Serializable

var baseurl = "http://10.0.0.79:8080"

// these functions are used to communicate auth requests to the spring backend

// login request
suspend fun loginUser(client: HttpClient, identifier: String, password: String): HttpStatusCode {
    try {
        // Send the login request to the server
        val authRequest: AuthRequest = if (isEmail(identifier)) { // email was entered
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
        val response: HttpResponse = client.post("$baseurl/api/auth/login") {
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
    val response: HttpResponse = client.post("$baseurl/api/auth/register") {
        contentType(ContentType.Application.Json)
        setBody(AuthRequest(username, password, email))
    }
    println(response)
    if (response.status == HttpStatusCode.OK) {
        val authToken = response.bodyAsText()
        Settings().putString("authToken", authToken)
        print("Registration successful")
        return response.status // successful registration

    } else {
        return response.status // failed to register
    }
}

@Serializable
data class AuthRequest(
    val username: String? = null,
    val password: String,
    val email: String? = null
)