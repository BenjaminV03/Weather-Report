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
        val townOrCity = when {
            !parsedResponse.address?.city.isNullOrEmpty() -> parsedResponse.address?.city + ", "
            !parsedResponse.address?.town.isNullOrEmpty() -> parsedResponse.address?.town + ", "
            else -> ""
        }
        val county = parsedResponse.address?.county.isNullOrEmpty().let { if (it) "" else parsedResponse.address?.county + ", "}
        val state = parsedResponse.address?.state.isNullOrEmpty().let { if (it) "" else parsedResponse.address?.state + ", "}
        val country = parsedResponse.address?.country.isNullOrEmpty().let { if (it) "" else parsedResponse.address?.country }
        val location = townOrCity + county + state + country
        location.ifEmpty { "Unknown location" }
    } catch (e: Exception) {
        println("Error fetching location: ${e.message}")
        ""
    }
}