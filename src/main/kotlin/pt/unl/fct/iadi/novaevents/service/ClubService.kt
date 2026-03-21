package pt.unl.fct.iadi.novaevents.service

import org.springframework.stereotype.Service
import pt.unl.fct.iadi.novaevents.model.Club
import pt.unl.fct.iadi.novaevents.model.ClubCategory

@Service
class ClubService {
    // Requirements: Pre-seeded list of 5 clubs
    private val clubs = listOf(
        Club(1, "Chess Club", "Master the board and improve your strategy.", ClubCategory.SOCIAL),
        Club(2, "Robotics Club", "The Robotics Club is the place to turn ideas into machines", ClubCategory.TECHNOLOGY),
        Club(3, "Photography Club", "Capturing moments through the lens.", ClubCategory.ARTS),
        Club(4, "Hiking & Outdoors Club", "Explore nature and local trails.", ClubCategory.SPORTS),
        Club(5, "Film Society", "Discussing and screening cinematic masterpieces.", ClubCategory.CULTURAL)
    )

    fun findAll(): List<Club> = clubs

    fun findById(id: Long): Club {
        // Services should throw domain exceptions like NoSuchElementException [cite: 1248-1250]
        return clubs.find { it.id == id }
            ?: throw NoSuchElementException("Club with id $id not found")
    }
}