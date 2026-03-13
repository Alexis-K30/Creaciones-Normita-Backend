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
 * Equivale a Program.cs en el proyecto .NET.
 * Configura JWT, CORS, rutas públicas/protegidas y el filtro de autenticación.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    /**
     * Equivale a:
     *   builder.Services.AddAuthentication().AddJwtBearer(...)
     *   builder.Services.AddAuthorization()
     *   app.UseAuthentication()
     *   app.UseAuthorization()
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Equivale a builder.Services.AddCors(...)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // CSRF desactivado porque usamos JWT (stateless)
            .csrf(AbstractHttpConfigurer::disable)

            // Equivale a [AllowAnonymous] en LoginController y RegisterController
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/auth/login",
                    "/api/auth/register",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/"
                ).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/productos", "/api/productos/**").permitAll()
                .anyRequest().authenticated()   // todo lo demás requiere JWT
            )

            // Sin sesión HTTP (stateless), igual que en .NET con JWT
            .sessionManagement(sm ->
                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Registrar nuestro filtro JWT antes del filtro de usuario/contraseña
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Equivale a:
     *   builder.Services.AddCors(options => {
     *       options.AddPolicy(name: corsPolicy, policy => {
     *           policy.WithOrigins(...).AllowAnyHeader().AllowAnyMethod().AllowCredentials();
     *       });
     *   });
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:5285",   // Blazor Client
                "https://localhost:7175",  // Blazor HTTPS
                "http://localhost:5123",   // Swagger Server
                "https://localhost:7057",  // Swagger HTTPS
                "http://localhost:3000",   // React/cliente web
                "http://localhost:8082",   // Esta misma API
                "http://localhost:5173/"   //-->React Vite
        )
        );
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);   // Necesario para cookies HttpOnly

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
