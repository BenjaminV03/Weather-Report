package httpRequests

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

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