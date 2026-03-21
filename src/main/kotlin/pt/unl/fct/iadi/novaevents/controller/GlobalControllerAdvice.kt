package pt.unl.fct.iadi.novaevents.controller

import jakarta.servlet.http.HttpServletResponse
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalControllerAdvice {

    @ExceptionHandler(java.util.NoSuchElementException::class)
    fun handleNotFound(ex: java.util.NoSuchElementException, model: Model, response: HttpServletResponse): String {
        response.status = HttpServletResponse.SC_NOT_FOUND
        model.addAttribute("errorMessage", ex.message ?: "The requested resource does not exist.")
        return "error/404"
    }
}