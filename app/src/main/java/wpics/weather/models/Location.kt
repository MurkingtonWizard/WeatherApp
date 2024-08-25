package wpics.weather.models

/**
 * This data class keeps track of daily details related to the current location.
 *
 * @version 1.0
 */
data class Location(
    val latitude: Double,
    val longitude: Double,
    val city: String,
    val state: String? = null,
    val country: String,
    var details: LocationDetails? = null
)