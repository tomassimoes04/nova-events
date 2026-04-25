package pt.unl.fct.iadi.novaevents.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.unl.fct.iadi.novaevents.model.AppUser
import pt.unl.fct.iadi.novaevents.model.Event
import pt.unl.fct.iadi.novaevents.model.EventType
import pt.unl.fct.iadi.novaevents.repository.ClubRepository
import pt.unl.fct.iadi.novaevents.repository.EventRepository
import pt.unl.fct.iadi.novaevents.weather.WeatherService
import java.time.LocalDate

@Service
class EventService(
    private val eventRepository: EventRepository,
    private val clubRepository: ClubRepository,
    private val weatherService: WeatherService
) {

    companion object {
        const val HIKING_CLUB_NAME = "Hiking & Outdoors Club"
    }

    fun findAll(): List<Event> = eventRepository.findFiltered(null, null, null, null)

    fun findFiltered(type: EventType?, clubId: Long?, from: LocalDate?, to: LocalDate?): List<Event> =
        eventRepository.findFiltered(type, clubId, from, to)

    fun findById(id: Long): Event = eventRepository.findById(id)
        .orElseThrow { NoSuchElementException("Event not found") }

    fun findByClub(clubId: Long): List<Event> = eventRepository.findByClubId(clubId)

    @Transactional
    fun create(clubId: Long, name: String, date: LocalDate, type: EventType,
               loc: String? = null, desc: String? = null, owner: AppUser): Event {
        if (eventRepository.existsByNameIgnoreCase(name)) {
            throw IllegalArgumentException("An event with this name already exists")
        }
        // Hiking & Outdoors Club rules: outdoor events need a location and cannot be created
        // while it is raining at that location.
        val club = clubRepository.findById(clubId)
            .orElseThrow { NoSuchElementException("Club with id $clubId not found") }
        if (club.name.equals(HIKING_CLUB_NAME, ignoreCase = true)) {
            if (loc.isNullOrBlank()) {
                throw IllegalArgumentException("Location is required for outdoor events")
            }
            if (weatherService.isRaining(loc) == true) {
                throw IllegalArgumentException(
                    "It is currently raining at \"$loc\" — outdoor events cannot be created in bad weather"
                )
            }
        }
        val event = Event(clubId = clubId, name = name, date = date, type = type,
            location = loc, description = desc, owner = owner)
        return eventRepository.save(event)
    }

    @Transactional
    fun update(id: Long, name: String, date: LocalDate, type: EventType, loc: String?, desc: String?) {
        val event = eventRepository.findById(id)
            .orElseThrow { NoSuchElementException("Event not found") }
        if (eventRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw IllegalArgumentException("An event with this name already exists")
        }
        event.name = name
        event.date = date
        event.type = type
        event.location = loc
        event.description = desc
        eventRepository.save(event)
    }

    @Transactional
    fun delete(id: Long) {
        eventRepository.deleteById(id)
    }

    fun isOwner(eventId: Long, username: String): Boolean =
        eventRepository.findById(eventId).map { it.owner?.username == username }.orElse(false)
}
