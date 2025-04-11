package httpRequests

import components.User

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.client.*

// these functions are used to access user data from the web servive via requsets handled by spring

// find user by email
suspend fun findUserByEmail(client: HttpClient, email : String): User {
    val user: User = client.get("$baseurl/api/user/email/$email"){
        accept(ContentType.Application.Json)
        header("Authorization", "Bearer ${getAuthToken()}")
    }.body()
    return user
}