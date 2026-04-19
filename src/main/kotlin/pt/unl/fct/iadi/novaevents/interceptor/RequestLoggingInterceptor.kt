package pt.unl.fct.iadi.novaevents.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class RequestLoggingInterceptor : HandlerInterceptor {

    private val log = LoggerFactory.getLogger(RequestLoggingInterceptor::class.java)

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        val auth = SecurityContextHolder.getContext().authentication
        val principal = if (auth != null && auth.isAuthenticated && auth.name != "anonymousUser") {
            auth.name
        } else {
            "anonymous"
        }
        log.info("[{}] {} {} [{}]", principal, request.method, request.requestURI, response.status)
    }
}
