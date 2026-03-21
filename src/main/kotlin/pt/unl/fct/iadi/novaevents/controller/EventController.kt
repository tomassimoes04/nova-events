package pt.unl.fct.iadi.novaevents.controller

import jakarta.validation.Valid
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import pt.unl.fct.iadi.novaevents.controller.dto.EventRequest
import pt.unl.fct.iadi.novaevents.service.EventService
import pt.unl.fct.iadi.novaevents.service.ClubService
import pt.unl.fct.iadi.novaevents.model.EventType
import java.time.LocalDate

@Controller
class EventController(
    private val eventService: EventService,
    private val clubService: ClubService
) {

    // US3: Lista Global de Eventos
    @GetMapping("/events")
    fun listAllEvents(
        @RequestParam(required = false) type: EventType?,
        @RequestParam(required = false) clubId: Long?,
        @RequestParam(required = false) from: LocalDate?,
        @RequestParam(required = false) to: LocalDate?,
        model: ModelMap
    ): String {
        var filteredEvents = eventService.findAll()

        if (type != null) filteredEvents = filteredEvents.filter { it.type == type }
        if (clubId != null) filteredEvents = filteredEvents.filter { it.clubId == clubId }
        if (from != null) filteredEvents = filteredEvents.filter { !it.date.isBefore(from) }
        if (to != null) filteredEvents = filteredEvents.filter { !it.date.isAfter(to) }

        model["events"] = filteredEvents
        model["clubs"] = clubService.findAll()
        return "events/list"
    }

    // US5: Mostrar formulário de criação
    @GetMapping("/clubs/{clubId}/events/new")
    fun showCreateForm(@PathVariable clubId: Long, model: ModelMap): String {
        model["eventRequest"] = EventRequest()
        model["clubId"] = clubId
        return "events/form"
    }

    // US5: Criar evento (POST)
    @PostMapping("/clubs/{clubId}/events")
    fun createEvent(
        @PathVariable clubId: Long,
        @Valid @ModelAttribute("eventRequest") form: EventRequest,
        bindingResult: BindingResult,
        model: ModelMap
    ): String {
        if (bindingResult.hasErrors()) {
            model["clubId"] = clubId
            return "events/form"
        }

        return try {
            eventService.create(clubId, form.name!!, form.date!!, form.type!!, form.location, form.description)
            "redirect:/clubs/$clubId" // POST-Redirect-GET [cite: 874, 1137]
        } catch (e: IllegalArgumentException) {
            bindingResult.rejectValue("name", "duplicate", e.message ?: "Invalid name")
            model["clubId"] = clubId
            return "events/form"
        }
    }

    // US6: Mostrar formulário de edição
    @GetMapping("/clubs/{clubId}/events/{eventId}/edit")
    fun showEditForm(@PathVariable clubId: Long, @PathVariable eventId: Long, model: ModelMap): String {
        val event = eventService.findById(eventId)
        model["eventRequest"] = EventRequest(event.name, event.date, event.type, event.location, event.description)
        model["clubId"] = clubId
        model["eventId"] = eventId
        return "events/form" // Reutiliza o formulário existente
    }

    // US6: Atualizar evento (PUT)
    @PutMapping("/clubs/{clubId}/events/{eventId}")
    fun updateEvent(
        @PathVariable clubId: Long,
        @PathVariable eventId: Long,
        @Valid @ModelAttribute("eventRequest") form: EventRequest,
        bindingResult: BindingResult,
        model: ModelMap // Adicionado para lidar com erros
    ): String {
        if (bindingResult.hasErrors()) {
            model["clubId"] = clubId
            model["eventId"] = eventId
            return "events/form" // Corrigido de "events/edit" para "events/form"
        }

        return try {
            eventService.update(eventId, form.name!!, form.date!!, form.type!!, form.location, form.description)
            "redirect:/clubs/$clubId"
        } catch (e: IllegalArgumentException) {
            bindingResult.rejectValue("name", "duplicate", e.message ?: "Invalid name")
            model["clubId"] = clubId
            model["eventId"] = eventId
            return "events/form"
        }
    }

    // US7: Confirmar eliminação
    @GetMapping("/clubs/{clubId}/events/{eventId}/delete")
    fun confirmDelete(@PathVariable clubId: Long, @PathVariable eventId: Long, model: ModelMap): String {
        model["event"] = eventService.findById(eventId)
        model["clubId"] = clubId
        return "events/delete-confirm"
    }

    // US7: Eliminar (DELETE)
    @DeleteMapping("/clubs/{clubId}/events/{eventId}")
    fun deleteEvent(@PathVariable clubId: Long, @PathVariable eventId: Long): String {
        eventService.delete(eventId)
        return "redirect:/clubs/$clubId"
    }
}