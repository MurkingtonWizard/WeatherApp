package wpics.weather.models

data class Forecast(
    // high and low temps
    var minTemperatureC: Double,
    var maxTemperatureC: Double,
    var minTemperatureF: Double,
    var maxTemperatureF: Double,
    // condition
    var condition: Condition
)
