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
 * Filtro de seguridad personalizado que se ejecuta una vez por cada petición HTTP.
 * Su responsabilidad es:
 * 1. Buscar un token JWT (en cookies o headers).
 * 2. Validar el token.
 * 3. Si es válido, establecer la autenticación del usuario en el contexto de seguridad de Spring.
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

        // 1️⃣ Intentar obtener el token desde la cookie "AuthToken"
        // Esto es útil para clientes web (navegadores) que almacenan el JWT en cookies HttpOnly.
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("AuthToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // 2️⃣ Si no hay cookie, buscar en el header "Authorization: Bearer <token>"
        // Esto es útil para clientes móviles, Postman o Swagger UI.
        if (token == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }

        // 3️⃣ Validar token y autenticar al usuario
        // Si encontramos un token y el usuario no está ya autenticado en el contexto actual:
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String username = jwtService.extractUsername(token);
                // Cargamos los detalles del usuario desde la base de datos
                UserDetails userDetails = userService.loadUserByUsername(username);

                // Verificamos si el token es válido (no expirado y firma correcta)
                if (jwtService.isTokenValid(token, userDetails)) {
                    // Creamos el objeto de autenticación
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // Establecemos la autenticación en el contexto de Spring Security
                    // Esto permite que los controladores sepan quién es el usuario actual.
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception ignored) {
                // Si el token es inválido o expiró, simplemente no autenticamos y dejamos pasar la petición.
                // Si la ruta requiere seguridad, Spring devolverá 403 más adelante.
            }
        }

        // Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }
}
