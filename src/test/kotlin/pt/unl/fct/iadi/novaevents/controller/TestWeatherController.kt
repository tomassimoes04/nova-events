package pt.unl.fct.iadi.novaevents.controller

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import pt.unl.fct.iadi.novaevents.weather.OpenWeatherClient
import pt.unl.fct.iadi.novaevents.weather.WeatherCondition
import pt.unl.fct.iadi.novaevents.weather.WeatherResponse

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = ["weather.api.key=test-key"])
class TestWeatherController {

    @TestConfiguration
    class FakeClientConfig {
        @Bean
        @Primary
        fun fakeOpenWeatherClient(): OpenWeatherClient = object : OpenWeatherClient {
            override fun getCurrentWeather(location: String, apiKey: String, units: String) =
                when (location.lowercase()) {
                    "rainytown" -> WeatherResponse(listOf(WeatherCondition(main = "Rain")))
                    "sunnyland" -> WeatherResponse(listOf(WeatherCondition(main = "Clear")))
                    else -> throw RuntimeException("upstream failure")
                }
        }
    }

    @Autowired lateinit var mvc: MockMvc

    @Test
    @WithMockUser
    fun `json returns raining true for rain`() {
        mvc.perform(get("/api/weather").param("location", "Rainytown").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.raining").value(true))
    }

    @Test
    @WithMockUser
    fun `json returns raining false for clear`() {
        mvc.perform(get("/api/weather").param("location", "Sunnyland").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.raining").value(false))
    }

    @Test
    @WithMockUser
    fun `json returns raining null on upstream failure`() {
        mvc.perform(get("/api/weather").param("location", "Elsewhere").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.raining").isEmpty)
    }

    @Test
    @WithMockUser
    fun `html fragment contains location`() {
        mvc.perform(get("/api/weather").param("location", "Rainytown").accept(MediaType.TEXT_HTML))
            .andExpect(status().isOk)
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Rainytown")))
    }

    @Test
    fun `api requires authentication`() {
        mvc.perform(get("/api/weather").param("location", "Rainytown").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized)
    }
}
