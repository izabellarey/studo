package de.cau.sp.stundenplanung.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        // >= 32 bytes for HS256.
        jwtService = new JwtService("01234567890123456789012345678901");
    }

    @Test
    void generatedTokenCanBeParsedAndContainsUsernameAndRole() {
        String token = jwtService.generateToken("mmustermann", "LEHRKRAFT");

        Claims claims = jwtService.parseToken(token);

        assertEquals("mmustermann", claims.getSubject());
        assertEquals("LEHRKRAFT", claims.get("role", String.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = jwtService.generateToken("mmustermann", "LEHRKRAFT");

        int mitte = token.length() / 2;
        char zeichen = token.charAt(mitte);
        char ersatz = (zeichen == 'a') ? 'b' : 'a';
        String tampered = token.substring(0, mitte) + ersatz + token.substring(mitte + 1);

        assertThrows(Exception.class, () -> jwtService.parseToken(tampered));
    }
}
