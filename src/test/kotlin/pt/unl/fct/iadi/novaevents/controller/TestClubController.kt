package pt.unl.fct.iadi.novaevents.controller

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.model
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.view

@SpringBootTest
@AutoConfigureMockMvc
class TestClubController {

    @Autowired lateinit var mvc: MockMvc

    @Test
    fun `root and clubs both render list view`() {
        mvc.perform(get("/")).andExpect(status().isOk).andExpect(view().name("clubs/list"))
        mvc.perform(get("/clubs")).andExpect(status().isOk)
            .andExpect(view().name("clubs/list"))
            .andExpect(model().attributeExists("clubs"))
    }

    @Test
    fun `club detail renders detail view with model attributes`() {
        mvc.perform(get("/clubs/1"))
            .andExpect(status().isOk)
            .andExpect(view().name("clubs/detail"))
            .andExpect(model().attributeExists("club"))
            .andExpect(model().attributeExists("events"))
    }

    @Test
    fun `event detail page is public`() {
        mvc.perform(get("/clubs/1/events/1"))
            .andExpect(status().isOk)
            .andExpect(view().name("events/detail"))
    }

    @Test
    fun `events list filters accept type and club`() {
        mvc.perform(get("/events").param("type", "WORKSHOP")).andExpect(status().isOk)
        mvc.perform(get("/events").param("club", "1")).andExpect(status().isOk)
        mvc.perform(get("/events").param("from", "2020-01-01").param("to", "2099-12-31"))
            .andExpect(status().isOk)
    }

    @Test
    fun `login page renders`() {
        mvc.perform(get("/login"))
            .andExpect(status().isOk)
            .andExpect(view().name("login"))
    }
}
