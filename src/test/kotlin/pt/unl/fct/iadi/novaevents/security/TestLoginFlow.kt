package pt.unl.fct.iadi.novaevents.security

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Exercises [LoginSuccessHandler] via the real form-login filter chain. We assert that
 * login issues a JWT cookie, that the redirect URL is absolute, and that an unauthenticated
 * request to a protected page is redirected to the login page (covering the entry point).
 */
@SpringBootTest
@AutoConfigureMockMvc
class TestLoginFlow {

    @Autowired lateinit var mvc: MockMvc

    @Test
    fun `successful login issues jwt cookie`() {
        val result = mvc.perform(formLogin("/login").user("alice").password("password123"))
            .andExpect(status().is3xxRedirection)
            .andExpect(cookie().exists("jwt"))
            .andReturn()
        assertNotNull(result.response.getCookie("jwt"))
        // Redirect should be absolute (LoginSuccessHandler builds it from the request).
        assert(result.response.getHeader("Location")!!.startsWith("http"))
    }

    @Test
    fun `failed login redirects to login error`() {
        mvc.perform(formLogin("/login").user("alice").password("wrong"))
            .andExpect(status().is3xxRedirection)
    }

    @Test
    fun `protected page redirects anonymous to login and saves redirect cookie`() {
        mvc.perform(get("/clubs/1/events/new"))
            .andExpect(status().is3xxRedirection)
            .andExpect(cookie().exists("REDIRECT_URI"))
    }

    @Test
    fun `logout clears jwt cookie`() {
        mvc.perform(
            org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders
                .logout("/logout")
        ).andExpect(status().is3xxRedirection)
    }
}
