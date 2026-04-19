package pt.unl.fct.iadi.novaevents.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date

@Service
class JwtService(
    @Value("\${jwt.secret:nova-events-secret-key-must-be-at-least-256-bits-long-for-hs256}") private val secret: String,
    @Value("\${jwt.expiration:86400000}") private val expiration: Long
) {
    private val key by lazy { Keys.hmacShaKeyFor(secret.toByteArray()) }

    fun generateToken(username: String, roles: List<String>): String =
        Jwts.builder()
            .subject(username)
            .claim("roles", roles)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expiration))
            .signWith(key)
            .compact()

    fun extractUsername(token: String): String? = getClaims(token)?.subject

    @Suppress("UNCHECKED_CAST")
    fun extractRoles(token: String): List<String> =
        getClaims(token)?.get("roles", List::class.java) as? List<String> ?: emptyList()

    fun isValid(token: String): Boolean = try {
        val claims = getClaims(token)
        claims != null && claims.expiration.after(Date())
    } catch (e: Exception) {
        false
    }

    private fun getClaims(token: String): Claims? = try {
        Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    } catch (e: Exception) {
        null
    }
}
