package pt.unl.fct.iadi.novaevents.controller

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import pt.unl.fct.iadi.novaevents.repository.ClubRepository
import pt.unl.fct.iadi.novaevents.weather.OpenWeatherClient
import pt.unl.fct.iadi.novaevents.weather.WeatherCondition
import pt.unl.fct.iadi.novaevents.weather.WeatherResponse

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = ["weather.api.key=test-key"])
class TestEventController {

    @TestConfiguration
    class FakeClientConfig {
        @Bean
        @Primary
        fun fakeOpenWeatherClient(): OpenWeatherClient = object : OpenWeatherClient {
            override fun getCurrentWeather(location: String, apiKey: String, units: String) =
                if (location.contains("Rain", ignoreCase = true))
                    WeatherResponse(listOf(WeatherCondition(main = "Rain")))
                else
                    WeatherResponse(listOf(WeatherCondition(main = "Clear")))
        }
    }

    @Autowired lateinit var mvc: MockMvc
    @Autowired lateinit var clubRepository: ClubRepository

    private fun hikingClubId() = clubRepository.findAll().first { it.name == "Hiking & Outdoors Club" }.id
    private fun chessClubId() = clubRepository.findAll().first { it.name == "Chess Club" }.id

    @Test
    fun `events list is public`() {
        mvc.perform(get("/events")).andExpect(status().isOk)
    }

    @Test
    fun `clubs list is public`() {
        mvc.perform(get("/clubs")).andExpect(status().isOk)
    }

    @Test
    @WithMockUser(username = "alice", roles = ["EDITOR"])
    fun `hiking without location shows error`() {
        mvc.perform(post("/clubs/${hikingClubId()}/events")
            .with(csrf())
            .param("name", "Test Hike One")
            .param("date", "2030-01-01")
            .param("type", "SOCIAL")
            .param("location", ""))
            .andExpect(status().isOk)
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Location is required")))
    }

    @Test
    @WithMockUser(username = "alice", roles = ["EDITOR"])
    fun `hiking rejected when raining`() {
        mvc.perform(post("/clubs/${hikingClubId()}/events")
            .with(csrf())
            .param("name", "Test Hike Rain")
            .param("date", "2030-01-01")
            .param("type", "SOCIAL")
            .param("location", "Rainville"))
            .andExpect(status().isOk)
            .andExpect(content().string(org.hamcrest.Matchers.containsString("raining")))
    }

    @Test
    @WithMockUser(username = "alice", roles = ["EDITOR"])
    fun `hiking accepted when dry`() {
        mvc.perform(post("/clubs/${hikingClubId()}/events")
            .with(csrf())
            .param("name", "Test Hike Dry")
            .param("date", "2030-01-01")
            .param("type", "SOCIAL")
            .param("location", "Drytown"))
            .andExpect(status().is3xxRedirection)
    }

    @Test
    @WithMockUser(username = "alice", roles = ["EDITOR"])
    fun `non-hiking has no location requirement`() {
        mvc.perform(post("/clubs/${chessClubId()}/events")
            .with(csrf())
            .param("name", "Test Chess New")
            .param("date", "2030-01-01")
            .param("type", "COMPETITION"))
            .andExpect(status().is3xxRedirection)
    }

    @Test
    fun `anonymous cannot POST event`() {
        mvc.perform(post("/clubs/${chessClubId()}/events")
            .with(csrf())
            .param("name", "x").param("date", "2030-01-01").param("type", "OTHER"))
            .andExpect(status().is3xxRedirection)
    }
}
