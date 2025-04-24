package httpRequests

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import com.russhwolf.settings.Settings

var baseurl = "http://10.0.0.79:8080"

// creates the client to the web service
fun getClient(): HttpClient {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json( Json {
                ignoreUnknownKeys = true // ignore extar fields in the json responce
                prettyPrint = true // debugging
                isLenient = true // lenient parsing
            })
        }
    }

    return client
}

// grabs the clients token if it exists
fun getAuthToken(): String? {
    val settings = Settings()
    return settings.getStringOrNull("authToken")
}

fun changeBaseUrl(url: String) { baseurl = url }