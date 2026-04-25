package pt.unl.fct.iadi.novaevents.weather

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class WeatherService(
    private val client: OpenWeatherClient,
    @Value("\${weather.api.key:}") private val apiKey: String
) {

    private val log = LoggerFactory.getLogger(WeatherService::class.java)

    /**
     * Returns `true` if it is currently raining at [location], `false` if it is not, or `null`
     * if the weather could not be determined (blank input, missing API key, upstream error).
     */
    fun isRaining(location: String?): Boolean? {
        if (location.isNullOrBlank()) return null
        if (apiKey.isBlank()) {
            log.warn("weather.api.key is not configured; treating weather as unknown")
            return null
        }
        return try {
            val response = client.getCurrentWeather(location, apiKey)
            response.weather.any { it.main.equals("Rain", ignoreCase = true) ||
                it.main.equals("Drizzle", ignoreCase = true) ||
                it.main.equals("Thunderstorm", ignoreCase = true) }
        } catch (ex: Exception) {
            log.warn("Failed to fetch weather for '{}': {}", location, ex.message)
            null
        }
    }
}
