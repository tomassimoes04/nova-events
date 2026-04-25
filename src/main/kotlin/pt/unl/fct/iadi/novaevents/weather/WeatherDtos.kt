package pt.unl.fct.iadi.novaevents.weather

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Subset of the OpenWeatherMap `/data/2.5/weather` response we care about.
 * We only look at the first element of `weather` to decide if it's raining.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class WeatherResponse(
    val weather: List<WeatherCondition> = emptyList(),
    val name: String? = null,
    val cod: Int? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class WeatherCondition(
    val id: Int = 0,
    val main: String = "",
    val description: String = "",
    val icon: String = ""
)
