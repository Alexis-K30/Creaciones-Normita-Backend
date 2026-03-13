package com.creacionesnormita.service;

import com.creacionesnormita.dto.RegisterRequest;
import com.creacionesnormita.entity.ApplicationUser;
import com.creacionesnormita.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Equivale a UserRegistrationService.cs del proyecto .NET
 * Además implementa UserDetailsService que Spring Security necesita
 * para cargar el usuario al validar un JWT.
 */
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Equivale a: _userManager.FindByNameAsync(username)
     * Spring Security llama este método automáticamente para autenticar.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado: " + username));
    }

    /**
     * Equivale a: _registrationService.RegisterUserAsync(request, User)
     * Hashea la contraseña con BCrypt (en .NET era PasswordHasher<ApplicationUser>).
     */
    public ApplicationUser registerUser(RegisterRequest request) {
        // Equivale a las validaciones de UserManager en .NET
        if (userRepository.existsByUsername(request.username()))
            throw new RuntimeException("El nombre de usuario ya existe");

        if (userRepository.existsByEmail(request.email()))
            throw new RuntimeException("El email ya está registrado");

        ApplicationUser user = new ApplicationUser();
        user.setUsername(request.username());
        user.setEmail(request.email());
        // BCrypt equivale al PasswordHasher de ASP.NET Identity
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setNombre(request.nombre());
        user.setDui(request.dui());
        user.setPais(request.pais());
        user.setRole("Usuario"); // Rol por defecto

        return userRepository.save(user);
    }
}
