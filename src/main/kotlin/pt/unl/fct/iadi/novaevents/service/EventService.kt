package pt.unl.fct.iadi.novaevents.service

import org.springframework.stereotype.Service
import pt.unl.fct.iadi.novaevents.model.Event
import pt.unl.fct.iadi.novaevents.model.EventType
import java.time.LocalDate

@Service
class EventService {
    private val events = mutableListOf<Event>()
    private var nextId = 1L

    init {
        // É crucial manter esta ordem para que os IDs batam certo com os testes
        create(1, "Beginner's Chess Workshop", LocalDate.now().plusDays(7), EventType.WORKSHOP)
        create(1, "Spring Chess Tournament", LocalDate.now().plusDays(14), EventType.COMPETITION)
        create(2, "Robot Build Night", LocalDate.now().plusDays(2), EventType.WORKSHOP)
    }

    fun findAll() = events.toList()

    fun findById(id: Long) = events.find { it.id == id }
        ?: throw NoSuchElementException("Event not found")

    fun findByClub(clubId: Long) = events.filter { it.clubId == clubId }

    fun create(clubId: Long, name: String, date: LocalDate, type: EventType, loc: String? = null, desc: String? = null): Event {
        // Requirement: Unique name check (case-insensitive) [cite: 1184-1188]
        if (events.any { it.name.equals(name, ignoreCase = true) }) {
            throw IllegalArgumentException("An event with this name already exists")
        }
        val event = Event(nextId++, clubId, name, date, type, loc, desc)
        events.add(event)
        return event
    }

    fun update(id: Long, name: String, date: LocalDate, type: EventType, loc: String?, desc: String?) {
        val index = events.indexOfFirst { it.id == id }
        if (index == -1) throw NoSuchElementException()

        // Ensure name uniqueness (excluding itself)
        if (events.any { it.id != id && it.name.equals(name, ignoreCase = true) }) {
            throw IllegalArgumentException("An event with this name already exists")
        }

        events[index] = events[index].copy(name = name, date = date, type = type, location = loc, description = desc)
    }

    fun delete(id: Long) {
        events.removeIf { it.id == id }
    }
}