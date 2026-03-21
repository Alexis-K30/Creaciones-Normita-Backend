package com.creacionesnormita.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Entidad que representa a un usuario en la base de datos (tabla 'users').
 * Implementa UserDetails, la interfaz estándar de Spring Security para manejar la autenticación.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationUser implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    // Contraseña encriptada
    @Column(nullable = false)
    private String password;

    private String nombre;
    private String pais;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    // Rol del usuario (ej: "Usuario", "Administrador")
    private String role = "Usuario";

    // ─── Métodos requeridos por UserDetails (Spring Security) ───────────────────

    /**
     * Devuelve los roles y permisos del usuario.
     * En este caso, convierte el rol simple a un formato que Spring Security entiende (ROLE_NombreRol).
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    // Métodos para controlar si la cuenta está expirada, bloqueada, etc.
    // Devolvemos true por defecto para mantener la cuenta siempre activa.
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }
}
