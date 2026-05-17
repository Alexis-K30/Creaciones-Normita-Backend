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
 * Servicio encargado de la gestión de usuarios.
 * Implementa UserDetailsService para que Spring Security pueda cargar usuarios desde la base de datos
 * durante el proceso de autenticación.
 */
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Carga un usuario por su nombre de usuario.
     * Este método es llamado automáticamente por Spring Security cuando necesita verificar credenciales.
     *
     * @param username El email a buscar.
     * @return UserDetails objeto que representa al usuario autenticado.
     * @throws UsernameNotFoundException Si el usuario no existe.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException(
                                "Usuario no encontrado: " + username)));
    }

    /**
     * Registra un nuevo usuario en la base de datos.
     * Realiza validaciones previas (nombre de usuario y email únicos) y encripta la contraseña.
     *
     * @param request Datos del usuario a registrar.
     * @return El usuario creado y guardado en la base de datos.
     * @throws RuntimeException Si el usuario o email ya existen.
     */
    public ApplicationUser registerUser(RegisterRequest request) {
        // Validar que el nombre de usuario no exista
        if (userRepository.existsByUsername(request.username()))
            throw new RuntimeException("El nombre de usuario ya existe");

        // Validar que el correo electrónico no exista
        if (userRepository.existsByEmail(request.email()))
            throw new RuntimeException("El email ya está registrado");

        ApplicationUser user = new ApplicationUser();
        user.setUsername(request.username());
        user.setEmail(request.email());
        
        // Encriptar la contraseña antes de guardarla (BCrypt)
        // Nunca se debe guardar la contraseña en texto plano.
        user.setPassword(passwordEncoder.encode(request.password()));
        
        user.setNombre(request.nombre());
        user.setPais(request.pais());
        
        // Asignar rol por defecto
        user.setRole("Usuario");

        return userRepository.save(user);
    }
}
