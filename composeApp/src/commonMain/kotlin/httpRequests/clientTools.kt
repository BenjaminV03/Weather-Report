package httpRequests

import components.Config

import com.russhwolf.settings.Settings
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.example.project.readConfigFile
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

var config: Config = readConfigFile("config.json").let { Json.decodeFromString(it) }
var baseurl = config.baseurl

// creates the client to the web service
fun getClient(): HttpClient {
    return HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true // Ignore extra fields in the JSON response
                prettyPrint = true // Debugging
                isLenient = true // Lenient parsing
            })
        }
        engine {
            https {
                trustManager = object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate>? = null
                }
            }
        }
    }
}

// grabs the clients token if it exists
fun getAuthToken(): String? {
    val settings = Settings()
    return settings.getStringOrNull("authToken")
}

fun changeBaseUrl(url: String) { baseurl = url }
