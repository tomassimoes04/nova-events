package pt.unl.fct.iadi.novaevents.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import pt.unl.fct.iadi.novaevents.service.ClubService
import pt.unl.fct.iadi.novaevents.service.EventService;


@Controller
class ClubController(private val clubService: ClubService
, private val eventService: EventService) {

    @GetMapping("/", "/clubs")
    fun listClubs(model: ModelMap): String {
        // model["key"] is the same as model.addAttribute("key", value) [cite: 1400]
        model["clubs"] = clubService.findAll()
        return "clubs/list" // This matches your template folder structure
    }

    @GetMapping("/clubs/{id}")
    fun clubDetail(@PathVariable id: Long, model: ModelMap): String {
        model["club"] = clubService.findById(id)
        model["events"] = eventService.findByClub(id) // Add this line
        return "clubs/detail"
    }
}