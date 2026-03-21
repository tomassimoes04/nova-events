package pt.unl.fct.iadi.novaevents.controller.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import pt.unl.fct.iadi.novaevents.model.EventType
import java.time.LocalDate

data class EventRequest(
    @field:NotBlank(message = "Name is required")
    val name: String? = null,

    @field:NotNull(message = "Date is required")
    val date: LocalDate? = null,

    @field:NotNull(message = "Event type is required")
    val type: EventType? = null,

    val location: String? = null,
    val description: String? = null
)