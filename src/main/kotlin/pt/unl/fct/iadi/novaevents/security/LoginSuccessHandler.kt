package pt.unl.fct.iadi.novaevents.security

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class LoginSuccessHandler(private val jwtService: JwtService) : AuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val username = authentication.name
        val roles = authentication.authorities.map { it.authority }
        val token = jwtService.generateToken(username, roles)

        val jwtCookie = Cookie("jwt", token).apply {
            isHttpOnly = true
            path = "/"
            maxAge = 86400
        }
        response.addCookie(jwtCookie)

        // Redirect to saved URL from cookie, or default to home
        val redirectUrl = request.cookies
            ?.find { it.name == "REDIRECT_URI" }
            ?.value
            ?.takeIf { it.isNotBlank() }
            ?: "/"

        // Clear the redirect cookie
        val clearRedirect = Cookie("REDIRECT_URI", "").apply {
            maxAge = 0
            path = "/"
        }
        response.addCookie(clearRedirect)

        response.sendRedirect(redirectUrl)
    }
}
