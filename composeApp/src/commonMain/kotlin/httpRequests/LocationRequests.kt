package httpRequests

import io.ktor.client.request.*
import io.ktor.client.*
import io.ktor.client.call.*
import kotlinx.serialization.Serializable

@Serializable
data class NominatimResponse(
    val address: Address? = null,
)

@Serializable
data class Address(
    val town: String? = "",
    val city: String? = "",
    val county: String? = null,
    val state: String? = null,
    val country: String? = null,
)


suspend fun getStateFromCoordinatesNominatim(client: HttpClient, latitude: Double, longitude: Double): String {
    val url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=$latitude&lon=$longitude"
    return try {
        val response: NominatimResponse = client.get(url).body()
        response.address?.state.toString()
    } catch (e: Exception) {
        println("Error fetching state: ${e.message}")
        ""
    }
}

suspend fun getLocationFromCoordinatesNominatim(client: HttpClient, latitude: Double, longitude: Double): String {
    val url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=$latitude&lon=$longitude"
    return try {
        val parsedResponse: NominatimResponse = client.get(url).body()
        val location =
            parsedResponse.address?.town.toString() + ", " +
            parsedResponse.address?.city.toString() + ", " +
            parsedResponse.address?.county.toString() + ", " +
            parsedResponse.address?.state.toString() + ", " +
            parsedResponse.address?.country.toString()
        println("Location: $location")
        location.ifEmpty { "Unknown location" }
    } catch (e: Exception) {
        println("Error fetching location: ${e.message}")
        ""
    }
}