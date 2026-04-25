package pt.unl.fct.iadi.novaevents.security

import jakarta.servlet.http.Cookie
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.csrf.CookieCsrfTokenRepository

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val jwtAuthFilter: JwtAuthFilter,
    private val loginSuccessHandler: LoginSuccessHandler
) {

    /**
     * API chain — matches ``/api/` paths` first. Stateless, JWT-only, no form login, CSRF disabled
     * (safe because the chain never authenticates cookie-only browser sessions for these endpoints).
     * Unauthenticated requests get `401` instead of a redirect to the login page.
     */
    @Bean
    @Order(1)
    fun apiSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher("/api/**")
            .authorizeHttpRequests { it.anyRequest().authenticated() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .csrf { it.disable() }
            .httpBasic { }
            .exceptionHandling { it.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }

    /**
     * Web chain — handles everything else (pages). Form login + JWT cookie, stateless.
     */
    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    fun webSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { auth ->
                auth.requestMatchers("/login", "/login/**", "/error", "/favicon.ico").permitAll()
                auth.requestMatchers(HttpMethod.GET, "/clubs/*/events/new").hasAnyRole("EDITOR", "ADMIN")
                auth.requestMatchers(HttpMethod.POST, "/clubs/*/events").hasAnyRole("EDITOR", "ADMIN")
                auth.requestMatchers(HttpMethod.GET, "/clubs/*/events/*/edit").hasAnyRole("EDITOR", "ADMIN")
                auth.requestMatchers(HttpMethod.PUT, "/clubs/*/events/*").hasAnyRole("EDITOR", "ADMIN")
                auth.requestMatchers(HttpMethod.GET, "/clubs/*/events/*/delete").hasRole("ADMIN")
                auth.requestMatchers(HttpMethod.DELETE, "/clubs/*/events/*").hasRole("ADMIN")
                auth.requestMatchers(HttpMethod.GET, "/", "/clubs", "/clubs/*", "/events").permitAll()
                auth.requestMatchers(HttpMethod.GET, "/clubs/*/events/*").permitAll()
                auth.anyRequest().authenticated()
            }
            .formLogin { form ->
                form.loginPage("/login")
                    .successHandler(loginSuccessHandler)
                    .failureUrl("/login?error")
                    .permitAll()
            }
            .logout { logout ->
                logout.logoutSuccessUrl("/")
                    .deleteCookies("jwt")
                    .clearAuthentication(true)
                    .permitAll()
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .csrf { it.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()) }
            .exceptionHandling { ex ->
                val loginEntryPoint = LoginUrlAuthenticationEntryPoint("/login")
                ex.authenticationEntryPoint { request, response, authException ->
                    val savedUrl = request.requestURI +
                        (request.queryString?.let { "?$it" } ?: "")
                    val redirectCookie = Cookie("REDIRECT_URI", savedUrl).apply {
                        path = "/"
                        maxAge = 300
                    }
                    response.addCookie(redirectCookie)
                    loginEntryPoint.commence(request, response, authException)
                }
            }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
