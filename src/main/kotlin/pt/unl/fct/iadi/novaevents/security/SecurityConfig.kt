package pt.unl.fct.iadi.novaevents.security

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
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

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { auth ->
                // Public infrastructure paths
                auth.requestMatchers("/login", "/login/**", "/error", "/favicon.ico").permitAll()
                // Restricted write operations must be declared BEFORE the broad GET permitAll rules
                // Creating events: EDITOR or ADMIN
                auth.requestMatchers(HttpMethod.GET, "/clubs/*/events/new").hasAnyRole("EDITOR", "ADMIN")
                auth.requestMatchers(HttpMethod.POST, "/clubs/*/events").hasAnyRole("EDITOR", "ADMIN")
                // Editing events: EDITOR or ADMIN (+ @PreAuthorize ownership check)
                auth.requestMatchers(HttpMethod.GET, "/clubs/*/events/*/edit").hasAnyRole("EDITOR", "ADMIN")
                auth.requestMatchers(HttpMethod.PUT, "/clubs/*/events/*").hasAnyRole("EDITOR", "ADMIN")
                // Deleting events: ADMIN only (+ @PreAuthorize ownership check)
                auth.requestMatchers(HttpMethod.GET, "/clubs/*/events/*/delete").hasRole("ADMIN")
                auth.requestMatchers(HttpMethod.DELETE, "/clubs/*/events/*").hasRole("ADMIN")
                // Public read operations (after specific restricted rules above)
                auth.requestMatchers(HttpMethod.GET, "/", "/clubs", "/clubs/*", "/events").permitAll()
                auth.requestMatchers(HttpMethod.GET, "/clubs/*/events/*").permitAll()
                // Everything else requires authentication
                auth.anyRequest().authenticated()
            }
            .formLogin { form ->
                form
                    .loginPage("/login")
                    .successHandler(loginSuccessHandler)
                    .failureUrl("/login?error")
                    .permitAll()
            }
            .logout { logout ->
                logout
                    .logoutSuccessUrl("/")
                    .deleteCookies("jwt")
                    .clearAuthentication(true)
                    .permitAll()
            }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .csrf { csrf ->
                csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            }
            .exceptionHandling { ex ->
                val loginEntryPoint = LoginUrlAuthenticationEntryPoint("/login")
                ex.authenticationEntryPoint { request, response, authException ->
                    // Save the requested URL in a cookie for post-login redirect
                    val savedUrl = request.requestURI +
                        (request.queryString?.let { "?$it" } ?: "")
                    val redirectCookie = Cookie("REDIRECT_URI", savedUrl).apply {
                        path = "/"
                        maxAge = 300
                    }
                    response.addCookie(redirectCookie)
                    // Delegate to Spring's standard entry point (sends absolute redirect URL)
                    loginEntryPoint.commence(request, response, authException)
                }
            }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
