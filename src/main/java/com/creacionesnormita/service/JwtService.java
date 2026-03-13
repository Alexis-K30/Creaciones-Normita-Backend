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
 * Equivale a JwtService.cs del proyecto .NET
 * Genera y valida tokens JWT.
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
     * Equivale a: _jwtService.GenerateToken(user, roles, out DateTime? expires)
     * En .NET los Administradores obtenían 1 año, los demás según configuración.
     */
    public String generateToken(ApplicationUser user) {
        long expirationMs = "Administrador".equalsIgnoreCase(user.getRole())
                ? 365L * 24 * 60 * 60 * 1000   // 1 año (igual que en .NET)
                : (long) expirationMinutes * 60 * 1000;

        return Jwts.builder()
                .subject(user.getUsername())                          // ClaimTypes.Name
                .issuer(issuer)                                       // ValidIssuer
                .audience().add(audience).and()                       // ValidAudience
                .claim("role", user.getRole())                        // ClaimTypes.Role
                .claim("dui", user.getDui())                          // claim "Dui"
                .claim("nombre", user.getNombre())
                .id(UUID.randomUUID().toString())                     // JwtRegisteredClaimNames.Jti
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Obtiene el tiempo de expiración del token (para la cookie).
     * Equivale al parámetro out DateTime? expires del .NET.
     */
    public Instant getExpiration(String token) {
        return parseClaims(token).getExpiration().toInstant();
    }

    /**
     * Equivale a la validación interna que hacía JwtBearer middleware en .NET
     */
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
