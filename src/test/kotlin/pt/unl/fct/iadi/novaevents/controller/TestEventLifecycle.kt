package pt.unl.fct.iadi.novaevents.controller

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.view
import pt.unl.fct.iadi.novaevents.repository.EventRepository

/**
 * Drives the full create / edit / delete flow over MockMvc to exercise EventController
 * authorization and form-binding paths beyond what TestEventController covers.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TestEventLifecycle {

    @Autowired lateinit var mvc: MockMvc
    @Autowired lateinit var eventRepository: EventRepository

    @Test
    @WithMockUser(username = "alice", roles = ["EDITOR"])
    fun `show create form`() {
        mvc.perform(get("/clubs/1/events/new"))
            .andExpect(status().isOk)
            .andExpect(view().name("events/form"))
    }

    @Test
    @WithMockUser(username = "alice", roles = ["EDITOR"])
    fun `create with validation error redisplays form`() {
        mvc.perform(post("/clubs/1/events").with(csrf())
            .param("name", "")
            .param("date", "2030-01-01")
            .param("type", "OTHER"))
            .andExpect(status().isOk)
            .andExpect(view().name("events/form"))
    }

    @Test
    @WithMockUser(username = "alice", roles = ["EDITOR"])
    fun `create with duplicate name redisplays form`() {
        // First create succeeds.
        mvc.perform(post("/clubs/1/events").with(csrf())
            .param("name", "Lifecycle Dup Event")
            .param("date", "2030-01-01")
            .param("type", "OTHER"))
            .andExpect(status().is3xxRedirection)
        // Second with same name surfaces as a binding error.
        mvc.perform(post("/clubs/1/events").with(csrf())
            .param("name", "Lifecycle Dup Event")
            .param("date", "2030-01-01")
            .param("type", "OTHER"))
            .andExpect(status().isOk)
            .andExpect(view().name("events/form"))
    }

    @Test
    @WithMockUser(username = "alice", roles = ["EDITOR"])
    fun `edit and update flow as owner`() {
        val created = eventRepository.findAll().first { it.owner?.username == "alice" }
        mvc.perform(get("/clubs/${created.clubId}/events/${created.id}/edit"))
            .andExpect(status().isOk)
        mvc.perform(put("/clubs/${created.clubId}/events/${created.id}").with(csrf())
            .param("name", created.name)
            .param("date", "2031-02-02")
            .param("type", "TALK")
            .param("location", "New Loc")
            .param("description", "updated"))
            .andExpect(status().is3xxRedirection)
    }

    @Test
    @WithMockUser(username = "charlie", roles = ["ADMIN"])
    fun `admin sees delete confirm and can delete`() {
        val created = eventRepository.findAll().first()
        mvc.perform(get("/clubs/${created.clubId}/events/${created.id}/delete"))
            .andExpect(status().isOk)
        mvc.perform(delete("/clubs/${created.clubId}/events/${created.id}").with(csrf()))
            .andExpect(status().is3xxRedirection)
    }

    @Test
    @WithMockUser(username = "alice", roles = ["EDITOR"])
    fun `non-admin cannot reach delete confirm`() {
        val created = eventRepository.findAll().first()
        mvc.perform(get("/clubs/${created.clubId}/events/${created.id}/delete"))
            .andExpect(status().isForbidden)
    }
}
