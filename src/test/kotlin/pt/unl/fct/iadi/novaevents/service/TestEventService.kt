package pt.unl.fct.iadi.novaevents.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*
import pt.unl.fct.iadi.novaevents.model.AppUser
import pt.unl.fct.iadi.novaevents.model.Club
import pt.unl.fct.iadi.novaevents.model.ClubCategory
import pt.unl.fct.iadi.novaevents.model.Event
import pt.unl.fct.iadi.novaevents.model.EventType
import pt.unl.fct.iadi.novaevents.repository.ClubRepository
import pt.unl.fct.iadi.novaevents.repository.EventRepository
import pt.unl.fct.iadi.novaevents.weather.WeatherService
import java.time.LocalDate
import java.util.Optional

class TestEventService {

    private val eventRepo = mock(EventRepository::class.java)
    private val clubRepo = mock(ClubRepository::class.java)
    private val weather = mock(WeatherService::class.java)
    private val service = EventService(eventRepo, clubRepo, weather)

    private val alice = AppUser(username = "alice", password = "x")
    private val chess = Club(id = 1, name = "Chess Club", description = "d", category = ClubCategory.SOCIAL)
    private val hiking = Club(id = 2, name = "Hiking & Outdoors Club", description = "d", category = ClubCategory.SPORTS)

    @Test
    fun `create saves event for non-hiking club`() {
        `when`(eventRepo.existsByNameIgnoreCase(anyString())).thenReturn(false)
        `when`(clubRepo.findById(1)).thenReturn(Optional.of(chess))
        `when`(eventRepo.save(any(Event::class.java))).thenAnswer { it.arguments[0] }

        val ev = service.create(1, "Tourney", LocalDate.now(), EventType.COMPETITION, owner = alice)
        assertEquals("Tourney", ev.name)
        verify(eventRepo).save(any(Event::class.java))
        verifyNoInteractions(weather)
    }

    @Test
    fun `duplicate name rejected`() {
        `when`(eventRepo.existsByNameIgnoreCase("dup")).thenReturn(true)
        assertThrows(IllegalArgumentException::class.java) {
            service.create(1, "dup", LocalDate.now(), EventType.OTHER, owner = alice)
        }
    }

    @Test
    fun `hiking requires location`() {
        `when`(eventRepo.existsByNameIgnoreCase(anyString())).thenReturn(false)
        `when`(clubRepo.findById(2)).thenReturn(Optional.of(hiking))
        val ex = assertThrows(IllegalArgumentException::class.java) {
            service.create(2, "Hike", LocalDate.now(), EventType.SOCIAL, loc = null, owner = alice)
        }
        assertEquals("Location is required for outdoor events", ex.message)
    }

    @Test
    fun `hiking rejects when raining`() {
        `when`(eventRepo.existsByNameIgnoreCase(anyString())).thenReturn(false)
        `when`(clubRepo.findById(2)).thenReturn(Optional.of(hiking))
        `when`(weather.isRaining("Sintra")).thenReturn(true)
        val ex = assertThrows(IllegalArgumentException::class.java) {
            service.create(2, "Hike", LocalDate.now(), EventType.SOCIAL, loc = "Sintra", owner = alice)
        }
        assert(ex.message!!.contains("raining"))
    }

    @Test
    fun `hiking allowed when dry`() {
        `when`(eventRepo.existsByNameIgnoreCase(anyString())).thenReturn(false)
        `when`(clubRepo.findById(2)).thenReturn(Optional.of(hiking))
        `when`(weather.isRaining("Sintra")).thenReturn(false)
        `when`(eventRepo.save(any(Event::class.java))).thenAnswer { it.arguments[0] }
        val ev = service.create(2, "Hike", LocalDate.now(), EventType.SOCIAL, loc = "Sintra", owner = alice)
        assertEquals("Sintra", ev.location)
    }

    @Test
    fun `hiking allowed when weather unknown`() {
        `when`(eventRepo.existsByNameIgnoreCase(anyString())).thenReturn(false)
        `when`(clubRepo.findById(2)).thenReturn(Optional.of(hiking))
        `when`(weather.isRaining("Elsewhere")).thenReturn(null)
        `when`(eventRepo.save(any(Event::class.java))).thenAnswer { it.arguments[0] }
        val ev = service.create(2, "Hike", LocalDate.now(), EventType.SOCIAL, loc = "Elsewhere", owner = alice)
        assertEquals("Elsewhere", ev.location)
    }

    @Test
    fun `update rejects duplicate name`() {
        `when`(eventRepo.findById(5L)).thenReturn(Optional.of(Event(id = 5, clubId = 1, name = "old")))
        `when`(eventRepo.existsByNameIgnoreCaseAndIdNot("new", 5)).thenReturn(true)
        assertThrows(IllegalArgumentException::class.java) {
            service.update(5, "new", LocalDate.now(), EventType.OTHER, null, null)
        }
    }

    @Test
    fun `update applies fields`() {
        val ev = Event(id = 5, clubId = 1, name = "old")
        `when`(eventRepo.findById(5L)).thenReturn(Optional.of(ev))
        `when`(eventRepo.existsByNameIgnoreCaseAndIdNot(anyString(), anyLong())).thenReturn(false)
        service.update(5, "new", LocalDate.of(2030, 1, 1), EventType.TALK, "loc", "desc")
        assertEquals("new", ev.name)
        assertEquals("loc", ev.location)
        verify(eventRepo).save(ev)
    }

    @Test
    fun `delete delegates to repo`() {
        service.delete(42)
        verify(eventRepo).deleteById(42)
    }

    @Test
    fun `isOwner checks owner username`() {
        val ev = Event(id = 1, clubId = 1, name = "x", owner = alice)
        `when`(eventRepo.findById(1L)).thenReturn(Optional.of(ev))
        assertEquals(true, service.isOwner(1, "alice"))
        assertEquals(false, service.isOwner(1, "bob"))
    }

    @Test
    fun `findById throws when missing`() {
        `when`(eventRepo.findById(99L)).thenReturn(Optional.empty())
        assertThrows(NoSuchElementException::class.java) { service.findById(99) }
    }
}
