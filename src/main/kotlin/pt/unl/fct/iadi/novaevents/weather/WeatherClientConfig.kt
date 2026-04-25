package pt.unl.fct.iadi.novaevents.weather

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

@Configuration
class WeatherClientConfig {

    /**
     * Builds the [OpenWeatherClient] proxy from an injected [RestClient.Builder]. The builder
     * is provided by Spring Boot auto-configuration, which means tests can replace it via a
     * `RestClientCustomizer` / `@TestConfiguration` without us ever constructing it statically.
     */
    @Bean
    fun openWeatherClient(
        builder: RestClient.Builder,
        @Value("\${weather.api.url:https://api.openweathermap.org}") baseUrl: String
    ): OpenWeatherClient {
        val restClient: RestClient = builder.baseUrl(baseUrl).build()
        val adapter = RestClientAdapter.create(restClient)
        val factory = HttpServiceProxyFactory.builderFor(adapter).build()
        return factory.createClient(OpenWeatherClient::class.java)
    }
}
