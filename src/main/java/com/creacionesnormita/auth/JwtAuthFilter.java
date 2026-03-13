package com.creacionesnormita.auth;

import com.creacionesnormita.service.JwtService;
import com.creacionesnormita.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Equivale al bloque OnMessageReceived en Program.cs (.NET):
 *
 *   options.Events = new JwtBearerEvents {
 *       OnMessageReceived = context => {
 *           if (context.Request.Cookies.ContainsKey("AuthToken"))
 *               context.Token = context.Request.Cookies["AuthToken"];
 *       }
 *   };
 *
 * En Spring esto se hace con un filtro que intercepta CADA request.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String token = null;

        // 1️⃣ Leer JWT desde la cookie "AuthToken" (igual que en .NET)
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("AuthToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // 2️⃣ Si no hay cookie, buscar en el header Authorization: Bearer <token>
        //    (para Swagger UI, igual que en .NET con AddSecurityDefinition "Bearer")
        if (token == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }

        // 3️⃣ Validar token y autenticar al usuario en el contexto de seguridad
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String username = jwtService.extractUsername(token);
                UserDetails userDetails = userService.loadUserByUsername(username);

                if (jwtService.isTokenValid(token, userDetails)) {
                    // Equivale a que JwtBearer middleware en .NET ponga el ClaimsPrincipal
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception ignored) {
                // Token inválido o expirado → el request seguirá sin autenticación
            }
        }

        filterChain.doFilter(request, response);
    }
}
