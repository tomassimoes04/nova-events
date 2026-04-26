package pt.unl.fct.iadi.novaevents

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import pt.unl.fct.iadi.novaevents.model.AppRole
import pt.unl.fct.iadi.novaevents.model.AppUser
import pt.unl.fct.iadi.novaevents.model.Club
import pt.unl.fct.iadi.novaevents.model.ClubCategory
import pt.unl.fct.iadi.novaevents.model.Event
import pt.unl.fct.iadi.novaevents.model.EventType
import pt.unl.fct.iadi.novaevents.repository.AppUserRepository
import pt.unl.fct.iadi.novaevents.repository.ClubRepository
import pt.unl.fct.iadi.novaevents.repository.EventRepository
import java.time.LocalDate

@Component
class DataInitializer(
    private val userRepository: AppUserRepository,
    private val clubRepository: ClubRepository,
    private val eventRepository: EventRepository,
    private val passwordEncoder: PasswordEncoder
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        // Seed users
        if (!userRepository.existsByUsername("alice")) {
            val alice = AppUser(username = "alice", password = passwordEncoder.encode("password123"))
            alice.roles.add(AppRole(name = "ROLE_EDITOR", user = alice))
            userRepository.save(alice)
        }
        if (!userRepository.existsByUsername("bob")) {
            val bob = AppUser(username = "bob", password = passwordEncoder.encode("password123"))
            bob.roles.add(AppRole(name = "ROLE_EDITOR", user = bob))
            userRepository.save(bob)
        }
        if (!userRepository.existsByUsername("charlie")) {
            val charlie = AppUser(username = "charlie", password = passwordEncoder.encode("password123"))
            charlie.roles.add(AppRole(name = "ROLE_ADMIN", user = charlie))
            userRepository.save(charlie)
        }

        // Seed clubs
        if (clubRepository.count() == 0L) {
            clubRepository.save(Club(name = "Chess Club", description = "Master the board and improve your strategy.", category = ClubCategory.SOCIAL))
            clubRepository.save(Club(name = "Robotics Club", description = "The Robotics Club is the place to turn ideas into machines.", category = ClubCategory.TECHNOLOGY))
            clubRepository.save(Club(name = "Photography Club", description = "Capturing moments through the lens.", category = ClubCategory.ARTS))
            clubRepository.save(Club(name = "Hiking & Outdoors Club", description = "Explore nature and local trails.", category = ClubCategory.SPORTS))
            clubRepository.save(Club(name = "Film Society", description = "Discussing and screening cinematic masterpieces.", category = ClubCategory.CULTURAL))
        }

        // Seed events (one per club minimum). We use the repository directly so we
        // bypass EventService's rain check, which would call the external weather API
        // and break tests that use MockRestServiceServer.
        if (eventRepository.count() == 0L) {
            val alice = userRepository.findByUsername("alice")!!
            val clubs = clubRepository.findAll()
            val clubMap = clubs.associateBy { it.name }

            val chessId = clubMap["Chess Club"]!!.id
            val roboticsId = clubMap["Robotics Club"]!!.id
            val photoId = clubMap["Photography Club"]!!.id
            val hikingId = clubMap["Hiking & Outdoors Club"]!!.id
            val filmId = clubMap["Film Society"]!!.id

            eventRepository.save(Event(clubId = chessId, name = "Beginner's Chess Workshop", date = LocalDate.now().plusDays(7), type = EventType.WORKSHOP, owner = alice))
            eventRepository.save(Event(clubId = chessId, name = "Spring Chess Tournament", date = LocalDate.now().plusDays(14), type = EventType.COMPETITION, owner = alice))
            eventRepository.save(Event(clubId = roboticsId, name = "Robot Build Night", date = LocalDate.now().plusDays(2), type = EventType.WORKSHOP, owner = alice))
            eventRepository.save(Event(clubId = photoId, name = "Street Photography Walk", date = LocalDate.now().plusDays(5), type = EventType.SOCIAL, owner = alice))
            eventRepository.save(Event(clubId = hikingId, name = "Serra da Arrábida Hike", date = LocalDate.now().plusDays(10), type = EventType.SOCIAL, location = "Setúbal", owner = alice))
            eventRepository.save(Event(clubId = filmId, name = "Kubrick Retrospective", date = LocalDate.now().plusDays(3), type = EventType.TALK, owner = alice))
        }
    }
}
