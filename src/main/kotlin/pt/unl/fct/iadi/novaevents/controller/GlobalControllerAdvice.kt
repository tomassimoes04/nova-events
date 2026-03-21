package pt.unl.fct.iadi.novaevents.controller

import org.springframework.http.HttpStatus
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class GlobalControllerAdvice {

    @ExceptionHandler(value = [NoSuchElementException::class])
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFound(ex: Exception, model: ModelMap): String {
        model["errorMessage"] = ex.message ?: "O recurso solicitado não existe."
        return "error/404"
    }
}