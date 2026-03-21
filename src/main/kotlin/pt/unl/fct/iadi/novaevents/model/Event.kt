package pt.unl.fct.iadi.novaevents.model

import java.time.LocalDate

enum class EventType { WORKSHOP, TALK, COMPETITION, SOCIAL, MEETING, OTHER }

data class Event(
    val id: Long,
    val clubId: Long,
    val name: String,
    val date: LocalDate,
    val type: EventType,
    val location: String? = null,
    val description: String? = null
)