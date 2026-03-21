package com.creacionesnormita.service;

import com.creacionesnormita.entity.ApplicationUser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Servicio encargado de la generación y validación de tokens JWT (JSON Web Tokens).
 * Utiliza la librería JJWT para firmar y verificar tokens.
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.audience}")
    private String audience;

    @Value("${jwt.expiration-minutes}")
    private int expirationMinutes;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Genera un token JWT para un usuario específico.
     * Incluye claims personalizados como rol y nombre.
     * Define una expiración diferente para administradores (1 año) y usuarios normales.
     *
     * @param user El usuario para el cual se genera el token.
     * @return El token JWT firmado como String.
     */
    public String generateToken(ApplicationUser user) {
        long expirationMs = "Administrador".equalsIgnoreCase(user.getRole())
                ? 365L * 24 * 60 * 60 * 1000   
                : (long) expirationMinutes * 60 * 1000;

        return Jwts.builder()
                .subject(user.getUsername())
                .issuer(issuer)
                .audience().add(audience).and()
                .claim("role", user.getRole())
                .claim("nombre", user.getNombre())
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public Instant getExpiration(String token) {
        return parseClaims(token).getExpiration().toInstant();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    private boolean isTokenExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
