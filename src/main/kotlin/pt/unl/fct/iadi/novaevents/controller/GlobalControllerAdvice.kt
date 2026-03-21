package pt.unl.fct.iadi.novaevents.controller

import org.springframework.http.HttpStatus
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import java.util.NoSuchElementException // IMPORT OBRIGATÓRIO

@ControllerAdvice
class GlobalControllerAdvice {

    @ExceptionHandler(NoSuchElementException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND) // Isto garante o código 404 nos testes
    fun handleNotFound(ex: NoSuchElementException, model: ModelMap): String {
        model["errorMessage"] = ex.message ?: "O recurso solicitado não foi encontrado."
        return "error/404"
    }
}