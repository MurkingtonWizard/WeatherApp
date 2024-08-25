package wpics.weather.fragments

import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import coil.load
import wpics.weather.R
import wpics.weather.models.Condition
import wpics.weather.models.Forecast
import wpics.weather.models.ForecastDay
import wpics.weather.models.Humidity
import wpics.weather.models.Location
import wpics.weather.models.LocationDetails
import wpics.weather.models.Precipitation
import wpics.weather.models.Pressure
import wpics.weather.models.Snow
import wpics.weather.models.Temperature
import wpics.weather.models.Wind
import wpics.weather.viewmodels.LoadViewModel
import java.net.URL
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * This fragment class will render all the weather information.
 *
 * @version 1.0
 */
class WeatherFragment : Fragment() {

    // Class attributes
    private lateinit var currentLoc: Location
    private lateinit var geocoder: Geocoder
    private lateinit var rootView: View
    private lateinit var unitSpinner: Spinner
    private var selectedUnit = "Imperial"
    private var imperial: Boolean = true

    private lateinit var today: ForecastDay
    private val forecast = mutableListOf<Forecast>()

    private val loadViewModel: LoadViewModel by lazy {
        ViewModelProvider(this)[LoadViewModel::class.java]
    }

    /**
     * This is the first method that gets called when the fragment starts
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_weather, container, false)

        unitSpinner = rootView.findViewById(R.id.idUnitSelector)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, arrayOf("Imperial","Metric","Hybrid"))
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        unitSpinner.adapter = adapter
        unitSpinner.setSelection(0)

        unitSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedUnit = parent.getItemAtPosition(position) as String
                imperial = selectedUnit == "Imperial"
                updateValues()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        if (container != null) {
            geocoder = Geocoder(container.context, Locale.getDefault())
        }

        // Load the arguments
        val args = arguments
        if (args != null) {
            args.getString("weatherAPIKey")?.let {
                getWeatherInfo(
                    args.getDouble("latitude"), args.getDouble("longitude"),
                    it
                )
            }
        }

        return rootView
    }

    @SuppressLint("SetTextI18n")
    fun updateValues() {
        if(!::today.isInitialized) return
        if(imperial) {
            rootView.findViewById<TextView>(R.id.idTemperature).text = today.temperature.temperatureF.toString()
            rootView.findViewById<TextView>(R.id.idPrecipitation).text = "${today.precipitation.precipitationIN} in"
            rootView.findViewById<TextView>(R.id.idPressure).text = "${today.pressure.pressureIN} in"

            rootView.findViewById<TextView>(R.id.idTodayTemps).text = "${today.temperature.maxTemperatureF}\n${today.temperature.minTemperatureF}"
            rootView.findViewById<TextView>(R.id.idDay1Temps).text = "${forecast[0].maxTemperatureF}\n${forecast[0].minTemperatureF}"
            rootView.findViewById<TextView>(R.id.idDay2Temps).text = "${forecast[1].maxTemperatureF}\n${forecast[1].minTemperatureF}"
            rootView.findViewById<TextView>(R.id.idDay3Temps).text = "${forecast[2].maxTemperatureF}\n${forecast[2].minTemperatureF}"
            rootView.findViewById<TextView>(R.id.idDay4Temps).text = "${forecast[3].maxTemperatureF}\n${forecast[3].minTemperatureF}"
            rootView.findViewById<TextView>(R.id.idDay5Temps).text = "${forecast[4].maxTemperatureF}\n${forecast[4].minTemperatureF}"
            rootView.findViewById<TextView>(R.id.idDay6Temps).text = "${forecast[5].maxTemperatureF}\n${forecast[5].minTemperatureF}"
        } else {
            rootView.findViewById<TextView>(R.id.idTemperature).text = today.temperature.temperatureC.toString()
            rootView.findViewById<TextView>(R.id.idPrecipitation).text = "${today.precipitation.precipitationMM} mm"
            rootView.findViewById<TextView>(R.id.idPressure).text = "${today.pressure.pressureMB} mb"

            rootView.findViewById<TextView>(R.id.idTodayTemps).text = "${today.temperature.maxTemperatureC}\n${today.temperature.minTemperatureC}"
            rootView.findViewById<TextView>(R.id.idDay1Temps).text = "${forecast[0].maxTemperatureC}\n${forecast[0].minTemperatureC}"
            rootView.findViewById<TextView>(R.id.idDay2Temps).text = "${forecast[1].maxTemperatureC}\n${forecast[1].minTemperatureC}"
            rootView.findViewById<TextView>(R.id.idDay3Temps).text = "${forecast[2].maxTemperatureC}\n${forecast[2].minTemperatureC}"
            rootView.findViewById<TextView>(R.id.idDay4Temps).text = "${forecast[3].maxTemperatureC}\n${forecast[3].minTemperatureC}"
            rootView.findViewById<TextView>(R.id.idDay5Temps).text = "${forecast[4].maxTemperatureC}\n${forecast[4].minTemperatureC}"
            rootView.findViewById<TextView>(R.id.idDay6Temps).text = "${forecast[5].maxTemperatureC}\n${forecast[5].minTemperatureC}"
        }

    }

    /**
     * This helper function uses the API to retrieve weather information
     *
     * @param latitude The latitude coordinate
     * @param longitude The longitude coordinate
     * @param weatherAPIKey The key for the weather API
     */
    private fun getWeatherInfo(latitude: Double, longitude: Double, weatherAPIKey: String) {
        // Obtain the current city, state (if any) and country information
        geocoder.getFromLocation(
            latitude,
            longitude,
            1
        ) { addresses ->
            // We found an address!
            if (addresses.isNotEmpty()) {
                currentLoc =
                    Location(
                        latitude,
                        longitude,
                        addresses[0].locality,
                        addresses[0].adminArea,
                        addresses[0].countryName
                    )

                // Fetch Astronomy Data
                loadViewModel.fetchAstronomyData(
                    "https://api.weatherapi.com/v1/",
                    weatherAPIKey,
                    "${currentLoc.latitude},${currentLoc.longitude}"
                )

                // Fetch Weather and Forecast Data
                loadViewModel.fetchWeatherResult(
                    "https://api.weatherapi.com/v1/",
                    weatherAPIKey,
                    "${currentLoc.latitude},${currentLoc.longitude}"
                )
            }
        }

        // Renders data when it is available
        renderData()
    }

    /**
     * This helper function renders the weather data.
     */
    @SuppressLint("SetTextI18n")
    private fun renderData() {
        // Render any astronomy data we have received
        loadViewModel.astronomyResult.observe(viewLifecycleOwner) {
            val jsonObject = loadViewModel.astronomyResult.value
            if (jsonObject != null) {
                val jsonAstronomyObject =
                    jsonObject.getJSONObject("astronomy").getJSONObject("astro")

                // Store the sunrise and sunset times
                val sunrise =
                    jsonAstronomyObject.getString("sunrise").replace("p", "PM").replace("a", "AM")
                val sunset =
                    jsonAstronomyObject.getString("sunset").replace("p", "PM").replace("a", "AM")
                val localDate = jsonObject.getJSONObject("location").getString("localtime")

                currentLoc.details = LocationDetails(
                    LocalTime.parse(
                        sunrise,
                        DateTimeFormatter.ofPattern("hh:mm a")
                    ),
                    LocalTime.parse(sunset, DateTimeFormatter.ofPattern("hh:mm a")),
                    LocalDateTime.parse(localDate, DateTimeFormatter.ofPattern("yyyy-MM-dd H:m"))
                )

                //TODO("Complete me by rendering this information!")

                val location = rootView.findViewById<TextView>(R.id.idLocation)
                location.text = currentLoc.city
                rootView.findViewById<TextView>(R.id.idSunrise).text = currentLoc.details!!.sunrise.toString()
                rootView.findViewById<TextView>(R.id.idSunset).text = currentLoc.details!!.sunset.toString()

                val days = arrayOf("Sun","Mon","Tue","Wed","Thu","Fri","Sat")
                val todayValue = currentLoc.details!!.lastUpdate.dayOfWeek.value

                rootView.findViewById<TextView>(R.id.idDay1Title).text = days[(todayValue+1)%7]
                rootView.findViewById<TextView>(R.id.idDay2Title).text = days[(todayValue+2)%7]
                rootView.findViewById<TextView>(R.id.idDay3Title).text = days[(todayValue+3)%7]
                rootView.findViewById<TextView>(R.id.idDay4Title).text = days[(todayValue+4)%7]
                rootView.findViewById<TextView>(R.id.idDay5Title).text = days[(todayValue+5)%7]
                rootView.findViewById<TextView>(R.id.idDay6Title).text = days[(todayValue+6)%7]
            }
        }

        // Render the current weather and 7-Day forecast data we have received
        loadViewModel.weatherResult.observe(viewLifecycleOwner) {

            //TODO("Complete me by storing the data in the Model layer!")
            val jsonObject = loadViewModel.weatherResult.value
            if (jsonObject != null) { 
                val jsonForecastObject = jsonObject.getJSONObject("forecast").getJSONArray("forecastday")
                val jsonCurrentObject = jsonObject.getJSONObject("current")

                val jsonForecast = jsonForecastObject.getJSONObject(0)
                val jsonDay = jsonForecast.getJSONObject("day")
                val jsonDayCondition = jsonCurrentObject.getJSONObject("condition")
                today = ForecastDay(
                    Condition(
                        jsonDayCondition.getString("text"),
                        URL("https:${jsonDayCondition.getString("icon")}"),
                        jsonDayCondition.getInt("code")),
                    Temperature(
                        jsonCurrentObject.getDouble("temp_c"),
                        jsonCurrentObject.getDouble("temp_f"),
                        jsonDay.getDouble("avgtemp_c"),
                        jsonDay.getDouble("avgtemp_f"),
                        jsonDay.getDouble("mintemp_c"),
                        jsonDay.getDouble("maxtemp_c"),
                        jsonDay.getDouble("mintemp_f"),
                        jsonDay.getDouble("maxtemp_f")),
                    Humidity(jsonCurrentObject.getDouble("humidity")),
                    Wind(
                        jsonCurrentObject.getDouble("wind_mph"),
                        jsonCurrentObject.getDouble("wind_kph"),
                        jsonCurrentObject.getDouble("wind_degree"),
                        jsonCurrentObject.getString("wind_dir")),
                    Precipitation(
                        jsonDay.getDouble("totalprecip_mm"),
                        jsonDay.getDouble("totalprecip_in")),
                    Pressure(
                        jsonCurrentObject.getDouble("pressure_mb"),
                        jsonCurrentObject.getDouble("pressure_in")
                    ),
                    Snow(jsonDay.getDouble("totalsnow_cm")))

                // next 6 day high, low, and condition
                for( i in 1 until jsonForecastObject.length()) {
                    val jsonCurrentDay = jsonForecastObject.getJSONObject(i).getJSONObject("day")
                    
                    val jsonCurrentCondition = jsonCurrentDay.getJSONObject("condition")
                    forecast.add ( Forecast (
                        jsonCurrentDay.getDouble("mintemp_c"),
                        jsonCurrentDay.getDouble("maxtemp_c"),
                        jsonCurrentDay.getDouble("mintemp_f"),
                        jsonCurrentDay.getDouble("maxtemp_f"),
                        Condition (
                            jsonCurrentCondition.getString("text"),
                            URL("https:${jsonCurrentCondition.getString("icon")}"),
                            jsonCurrentCondition.getInt("code")
                        )))
                }

                //TODO("Use the renderWeatherIcon function to download and render the icon")

                //TODO!!! Add units
                renderWeatherIcon(today.condition.icon.toString(),rootView.findViewById(R.id.idWeatherIcon))
                updateValues()
                rootView.findViewById<TextView>(R.id.idHumidity).text = "${today.humidity.humidity} %"

                //add icons
                renderWeatherIcon(today.condition.icon.toString(),rootView.findViewById(R.id.idTodayIcon))
                renderWeatherIcon(forecast[0].condition.icon.toString(),rootView.findViewById(R.id.idDay1Icon))
                renderWeatherIcon(forecast[1].condition.icon.toString(),rootView.findViewById(R.id.idDay2Icon))
                renderWeatherIcon(forecast[2].condition.icon.toString(),rootView.findViewById(R.id.idDay3Icon))
                renderWeatherIcon(forecast[3].condition.icon.toString(),rootView.findViewById(R.id.idDay4Icon))
                renderWeatherIcon(forecast[4].condition.icon.toString(),rootView.findViewById(R.id.idDay5Icon))
                renderWeatherIcon(forecast[5].condition.icon.toString(),rootView.findViewById(R.id.idDay6Icon))


                //TODO("Complete me by rendering all relevant information using the Model layer!")

                setBackground()

            }

        }
    }

    private fun setBackground() {
        val text = today.condition.text.lowercase(Locale.getDefault())
        val updateTime = currentLoc.details!!.lastUpdate.toLocalTime()
        Log.d("ConditionText", text)
        Log.d("TIME", updateTime.toString())

        val day = currentLoc.details!!.sunrise.isBefore(updateTime) &&
                currentLoc.details!!.sunset.isAfter(updateTime)
        Log.d("TIMEisDay", day.toString())

        var textColor = resources.getColor(R.color.black)
        if("snow" in text) {
            rootView.setBackgroundResource(R.color.snow)
        } else if ("rain" in text || "drizzle" in text) {
            rootView.setBackgroundResource(R.color.rain)
            textColor = resources.getColor(R.color.white)
        } else if ("sleet" in text) {
            rootView.setBackgroundResource(R.color.sleet)
        } else if ("fog" in text || "mist" in text) {
            rootView.setBackgroundResource(R.color.fog)
        } else if ("cloudy" in text || "overcast" in text) {
            if ("partly" in text) {
                if (day) {
                    rootView.setBackgroundResource(R.color.partly_cloudy_day)
                } else {
                    rootView.setBackgroundResource(R.color.partly_cloudy_night)
                }
            } else
                rootView.setBackgroundResource(R.color.cloudy)
        } else if ("sunny" in text) {
            rootView.setBackgroundResource(R.color.clear_day)
        } else if ("clear" in text) {
            if(day)
                rootView.setBackgroundResource(R.color.clear_day)
            else {
                rootView.setBackgroundResource(R.color.clear_night)
                textColor = resources.getColor(R.color.white)
            }
        } else {
            Log.d("BACKGROUND", "No background color, defaulting to clear day")
            rootView.setBackgroundResource(R.color.clear_day)
        }
        rootView.findViewById<GridLayout>(R.id.idForecastGrid).setBackgroundResource(R.color.white)
        rootView.findViewById<GridLayout>(R.id.idCurrentWeatherGrid).setBackgroundResource(R.color.white)

        setTextColor(textColor)
    }

    private fun setTextColor(color: Int) {
        val textViewIds = listOf(
            R.id.idLocation,
            R.id.idSunriseTitle,
            R.id.idSunsetTitle,
            R.id.idSunrise,
            R.id.idSunset,
            R.id.idForecast,
            R.id.idPrecipitationTitle,
            R.id.idPressureTitle,
            R.id.idHumidityTitle,
            R.id.idPrecipitation,
            R.id.idPressure,
            R.id.idHumidity
        )

        for (id in textViewIds) {
            rootView.findViewById<TextView>(id)?.setTextColor(color)
        }
        val spinnerText = rootView.findViewById<Spinner>(R.id.idUnitSelector).selectedView as TextView?
        spinnerText?.setTextColor(color)
    }

    /**
     * This helper function downloads the image using the URL provided by the weather API
     * and render it in an [ImageView] object.
     *
     * @param url The url where the icon is located
     * @param imageView The [ImageView] object where the icon will be displayed
     */
    private fun renderWeatherIcon(url: String, imageView: ImageView) {
        val formattedURL = if (url.startsWith("https:")) url else "https:$url"
        Log.d("URL", formattedURL)
        // Download the image asynchronously
        imageView.load(formattedURL) {
            error(R.drawable.ic_launcher_foreground)
        }
    }
}