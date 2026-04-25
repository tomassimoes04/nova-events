package pt.unl.fct.iadi.novaevents.weather

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * Unit tests for [WeatherService]. The REST client is a hand-rolled fake so we don't need
 * the Spring context or Mockito for the parts that should be pure logic.
 */
class TestWeatherService {

    private class FakeClient(private val response: WeatherResponse? = null,
                             private val throwOnCall: Boolean = false) : OpenWeatherClient {
        var lastLocation: String? = null
        var lastKey: String? = null
        override fun getCurrentWeather(location: String, apiKey: String, units: String): WeatherResponse {
            lastLocation = location
            lastKey = apiKey
            if (throwOnCall) throw RuntimeException("upstream boom")
            return response ?: WeatherResponse()
        }
    }

    @Test
    fun `blank location returns null without calling client`() {
        val fake = FakeClient()
        val svc = WeatherService(fake, "key")
        assertNull(svc.isRaining(""))
        assertNull(svc.isRaining(null))
        assertNull(fake.lastLocation)
    }

    @Test
    fun `missing api key returns null`() {
        val fake = FakeClient()
        val svc = WeatherService(fake, "")
        assertNull(svc.isRaining("Lisbon"))
        assertNull(fake.lastLocation)
    }

    @Test
    fun `rainy main condition yields true`() {
        val fake = FakeClient(WeatherResponse(listOf(WeatherCondition(main = "Rain"))))
        val svc = WeatherService(fake, "key")
        assertEquals(true, svc.isRaining("Lisbon"))
        assertEquals("Lisbon", fake.lastLocation)
    }

    @Test
    fun `drizzle and thunderstorm also count as rain`() {
        val drizzle = WeatherService(FakeClient(WeatherResponse(listOf(WeatherCondition(main = "Drizzle")))), "k")
        val thunder = WeatherService(FakeClient(WeatherResponse(listOf(WeatherCondition(main = "Thunderstorm")))), "k")
        assertEquals(true, drizzle.isRaining("A"))
        assertEquals(true, thunder.isRaining("A"))
    }

    @Test
    fun `clear skies yields false`() {
        val fake = FakeClient(WeatherResponse(listOf(WeatherCondition(main = "Clouds"))))
        val svc = WeatherService(fake, "key")
        assertEquals(false, svc.isRaining("Lisbon"))
    }

    @Test
    fun `upstream error is swallowed to null`() {
        val fake = FakeClient(throwOnCall = true)
        val svc = WeatherService(fake, "key")
        assertNull(svc.isRaining("Lisbon"))
    }
}
