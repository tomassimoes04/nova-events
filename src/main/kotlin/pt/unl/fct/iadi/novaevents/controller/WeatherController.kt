package pt.unl.fct.iadi.novaevents.controller

import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import pt.unl.fct.iadi.novaevents.weather.WeatherService

data class RainingResponse(val raining: Boolean?)

@Controller
@RequestMapping("/api/weather")
class WeatherController(private val weatherService: WeatherService) {

    /**
     * JSON flavour: `{ "raining": true | false | null }`.
     */
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun weatherJson(@RequestParam location: String): RainingResponse =
        RainingResponse(weatherService.isRaining(location))

    /**
     * HTML flavour: a Thymeleaf fragment rendering a coloured badge. Served when the browser
     * asks for `text/html` (htmx default).
     */
    @GetMapping(produces = [MediaType.TEXT_HTML_VALUE])
    fun weatherHtml(@RequestParam location: String, model: ModelMap): String {
        model["raining"] = weatherService.isRaining(location)
        model["location"] = location
        return "fragments/weather :: badge"
    }
}
