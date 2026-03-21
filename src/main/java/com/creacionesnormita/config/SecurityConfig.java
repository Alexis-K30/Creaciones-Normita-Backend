package com.creacionesnormita.config;

import com.creacionesnormita.auth.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Clase principal de configuración de seguridad de Spring.
 * Define cómo se manejan la autenticación, autorización, CORS y la seguridad de las rutas.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Habilita el uso de anotaciones como @PreAuthorize en los controladores
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    /**
     * Define la cadena de filtros de seguridad que se aplica a todas las peticiones HTTP.
     * Aquí se configuran las reglas de acceso a las rutas (endpoints).
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Configurar la política de Cross-Origin Resource Sharing (CORS)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Desactivar Cross-Site Request Forgery (CSRF)
            // No es necesario en APIs stateless que usan tokens JWT.
            .csrf(AbstractHttpConfigurer::disable)

            // Definir reglas de autorización para las rutas
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas que no requieren autenticación
                .requestMatchers(
                    "/api/auth/login",
                    "/api/auth/register",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/"
                ).permitAll()
                // Permitir peticiones GET a la API de productos para cualquier usuario
                .requestMatchers(HttpMethod.GET, "/api/productos", "/api/productos/**").permitAll()
                // Cualquier otra petición debe ser autenticada
                .anyRequest().authenticated()
            )

            // Configurar la gestión de sesiones como STATELESS
            // La API no mantendrá estado de sesión en el servidor; cada petición se valida con el token.
            .sessionManagement(sm ->
                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Añadir nuestro filtro personalizado (JwtAuthFilter) antes del filtro estándar de Spring
            // para procesar el JWT en cada petición.
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     ** CORS (Cross-Origin Resource Sharing) Configuration.**

     * Configura la política de CORS para permitir peticiones desde orígenes específicos.
     * Es fundamental para que los clientes web (ej. React, Blazor) puedan consumir la API.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Lista de orígenes permitidos para hacer peticiones a esta API
        config.setAllowedOrigins(List.of(
            "http://localhost:5285",   // Cliente Blazor
            "https://localhost:7175",  // Cliente Blazor (HTTPS)
            "http://localhost:5123",   // Swagger UI
            "https://localhost:7057",  // Swagger UI (HTTPS)
            "http://localhost:3000",   // Cliente React/Angular/Vue
            "http://localhost:8082",    // La propia API (para auto-referencias)
            "http://localhost:5173"    //Frontend de Vite/React
        ));
        // Métodos HTTP permitidos (GET, POST, etc.)
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Cabeceras HTTP permitidas (ej. "Authorization", "Content-Type")
        config.setAllowedHeaders(List.of("*"));
        // Permitir el envío de credenciales (cookies, tokens) en las peticiones
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Aplicar esta configuración a todas las rutas de la API
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Expone el AuthenticationManager de Spring como un Bean.
     * Es necesario para el proceso de autenticación manual en el AuthController.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
