package pt.unl.fct.iadi.novaevents

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import pt.unl.fct.iadi.novaevents.model.AppRole
import pt.unl.fct.iadi.novaevents.model.AppUser
import pt.unl.fct.iadi.novaevents.model.EventType
import pt.unl.fct.iadi.novaevents.repository.AppUserRepository
import pt.unl.fct.iadi.novaevents.service.EventService
import java.time.LocalDate

@Component
class DataInitializer(
    private val userRepository: AppUserRepository,
    private val eventService: EventService,
    private val passwordEncoder: PasswordEncoder
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
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

        if (eventService.findAll().isEmpty()) {
            val alice = userRepository.findByUsername("alice")!!
            eventService.create(1, "Beginner's Chess Workshop", LocalDate.now().plusDays(7), EventType.WORKSHOP, owner = alice)
            eventService.create(1, "Spring Chess Tournament", LocalDate.now().plusDays(14), EventType.COMPETITION, owner = alice)
            eventService.create(2, "Robot Build Night", LocalDate.now().plusDays(2), EventType.WORKSHOP, owner = alice)
        }
    }
}
