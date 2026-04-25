package pt.unl.fct.iadi.novaevents.security

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TestJwtService {

    private val service = JwtService(
        secret = "test-secret-key-that-is-long-enough-for-hs256-signing-please",
        expiration = 60_000
    )

    @Test
    fun `roundtrip token preserves username and roles`() {
        val token = service.generateToken("alice", listOf("ROLE_EDITOR", "ROLE_USER"))
        assertEquals("alice", service.extractUsername(token))
        assertEquals(listOf("ROLE_EDITOR", "ROLE_USER"), service.extractRoles(token))
        assertTrue(service.isValid(token))
    }

    @Test
    fun `invalid token returns null username and empty roles`() {
        assertNull(service.extractUsername("not-a-jwt"))
        assertEquals(emptyList<String>(), service.extractRoles("not-a-jwt"))
        assertFalse(service.isValid("not-a-jwt"))
    }

    @Test
    fun `expired token is not valid`() {
        val expiredService = JwtService(
            secret = "test-secret-key-that-is-long-enough-for-hs256-signing-please",
            expiration = -1
        )
        val token = expiredService.generateToken("bob", emptyList())
        assertFalse(expiredService.isValid(token))
    }
}
