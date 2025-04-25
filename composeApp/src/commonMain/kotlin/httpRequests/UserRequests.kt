package httpRequests

import components.User

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.client.*
import io.ktor.client.statement.*
import java.util.*

// these functions are used to access user data from the web servive via requsets handled by spring

// find user by email
suspend fun findUserByEmail(client: HttpClient, email : String): User {
    val user: User = client.get("$baseurl/api/users/email/$email"){
        accept(ContentType.Application.Json)
        header("Authorization", "Bearer ${getAuthToken()}")
    }.body()
    return user
}

// Find user by username
suspend fun findUserByUsername(client: HttpClient, username: String): User {
    println("Grabbing username from server")
    val user: User = client.get("$baseurl/api/users/$username") {
        accept(ContentType.Application.Json)
        header("Authorization", "Bearer ${getAuthToken()}")
    }.body()
    return user
}

// Update user password
suspend fun updatePassword(client: HttpClient, userId: UUID, oldPassword: String, newPassword: String): Result<String> {
    return try {
        val response = client.put("$baseurl/api/users/$userId/update-password") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer ${getAuthToken()}")
            setBody(
                mapOf(
                    "oldPassword" to oldPassword,
                    "newPassword" to newPassword
                )
            )
        }

        if (response.status == HttpStatusCode.OK) {
            Result.success("Password updated successfully")
        } else {
            val errorMessage = response.bodyAsText() // Extract error message from the response body
            Result.failure(Exception(errorMessage))
        }
    } catch (e: Exception) {
        Result.failure(e) // Handle network or unexpected errors
    }
}

// Update email
suspend fun updateEmail(client: HttpClient, userId: UUID, newEmail: String): Result<String> {
    return try {
        val response = client.put("$baseurl/api/users/$userId/update-email") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer ${getAuthToken()}")
            setBody(
                "newEmail" to newEmail,
            )
        }

        if (response.status == HttpStatusCode.OK) {
            Result.success("Email updated successfully")
        } else {
            val errorMessage = response.bodyAsText() // Extract error message from the response body
            Result.failure(Exception(errorMessage))
        }
    } catch (e: Exception) {
        Result.failure(e) // Handle network or unexpected errors
    }
}