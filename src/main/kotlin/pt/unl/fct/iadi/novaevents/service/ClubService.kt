package pt.unl.fct.iadi.novaevents.service

import org.springframework.stereotype.Service
import pt.unl.fct.iadi.novaevents.model.Club
import pt.unl.fct.iadi.novaevents.repository.ClubRepository

@Service
class ClubService(private val clubRepository: ClubRepository) {

    fun findAll(): List<Club> = clubRepository.findAllWithEvents()

    fun findById(id: Long): Club = clubRepository.findById(id)
        .orElseThrow { NoSuchElementException("Club with id $id not found") }
}
