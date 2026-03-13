package com.creacionesnormita.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Equivale a ApplicationUser.cs en el proyecto .NET
 * Extiende IdentityUser pero en Spring implementamos UserDetails directamente.
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

    // Equivale a IdentityUser.UserName
    @Column(unique = true, nullable = false)
    private String username;

    // Equivale a IdentityUser.Email
    @Column(unique = true, nullable = false)
    private String email;

    // Hash de la contraseña (BCrypt, equivale a PasswordHash en IdentityUser)
    @Column(nullable = false)
    private String password;

    // Campos custom — igual que en ApplicationUser.cs
    private String dui;
    private String nombre;
    private String pais;

    // Equivale a FechaCreacion = DateTime.UtcNow
    @Column(nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    // Equivale al IdentityRole (simplificado en un string)
    private String role = "Usuario";

    // ─── Métodos requeridos por UserDetails (Spring Security) ───────────────────

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }
}
