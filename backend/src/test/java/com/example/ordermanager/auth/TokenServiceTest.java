package com.example.ordermanager.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TokenServiceTest {

    private static final String SECRET = "12345678901234567890123456789012";
    private static final long EXPIRATION = 3_600_000L;

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "secret", SECRET);
        ReflectionTestUtils.setField(tokenService, "expiration", EXPIRATION);
    }

    @Test
    void generateToken_withValidEmail_setsEmailAsSubject() {
        String token = tokenService.generateToken("user@example.com");

        Claims claims = parseClaims(token, SECRET);

        assertEquals("user@example.com", claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void getEmailFromToken_withGeneratedToken_returnsEmail() {
        String token = tokenService.generateToken("user@example.com");

        String email = tokenService.getEmailFromToken(token);

        assertEquals("user@example.com", email);
    }

    @Test
    void generateToken_setsExpirationBasedOnConfiguredValue() {
        String token = tokenService.generateToken("user@example.com");

        Claims claims = parseClaims(token, SECRET);
        long duration = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();

        assertEquals(EXPIRATION, duration);
    }

    @Test
    void getEmailFromToken_withTokenSignedWithDifferentSecret_throwsJwtException() {
        String differentSecret = "abcdefghijklmnopqrstuvwxyz123456";
        String token = Jwts.builder()
                .setSubject("user@example.com")
                .signWith(Keys.hmacShaKeyFor(differentSecret.getBytes()), SignatureAlgorithm.HS256)
                .compact();

        assertThrows(JwtException.class, () -> tokenService.getEmailFromToken(token));
    }

    private Claims parseClaims(String token, String secret) {
        return Jwts.parserBuilder()
                .setSigningKey(secret.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}