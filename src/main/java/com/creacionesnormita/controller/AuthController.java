package com.creacionesnormita.controller;

import com.creacionesnormita.dto.LoginRequest;
import com.creacionesnormita.dto.LoginResponse;
import com.creacionesnormita.dto.RegisterRequest;
import com.creacionesnormita.entity.ApplicationUser;
import com.creacionesnormita.service.JwtService;
import com.creacionesnormita.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador para manejar la autenticación de usuarios.
 * Proporciona endpoints para iniciar sesión, registrarse y cerrar sesión.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Inicia sesión de un usuario.
     * Verifica las credenciales, genera un token JWT y lo devuelve en una cookie segura.
     *
     * @param request  Objeto con nombre de usuario y contraseña.
     * @param response Objeto de respuesta HTTP para establecer la cookie.
     * @return Respuesta con el token y detalles del usuario si el login es exitoso.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request,
                                   HttpServletResponse response) {
        try {
            // Cargar usuario desde la base de datos
            ApplicationUser user = (ApplicationUser)
                    userService.loadUserByUsername(request.email());

            // Verificar si la contraseña proporcionada coincide con la almacenada (hasheada)
            if (!passwordEncoder.matches(request.password(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Usuario o contraseña incorrecta");
            }

            // Generar token JWT para el usuario autenticado
            String token = jwtService.generateToken(user);
            var expiration = jwtService.getExpiration(token);

            // Crear cookie segura para almacenar el token
            // HttpOnly: true -> Evita acceso desde JavaScript (protección XSS)
            // Secure: false -> Permitir HTTP en desarrollo (cambiar a true en producción con HTTPS)
            Cookie cookie = new Cookie("AuthToken", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(false); 
            cookie.setPath("/");
            // Duración de la cookie: 1 año para admins, 1 hora para usuarios normales
            cookie.setMaxAge(
                "Administrador".equalsIgnoreCase(user.getRole())
                    ? 365 * 24 * 60 * 60   
                    : 60 * 60              
            );
            response.addCookie(cookie);

            // Devolver respuesta exitosa con el token y datos del usuario
            return ResponseEntity.ok(new LoginResponse(
                    token,
                    user.getUsername(),
                    user.getNombre(),
                    user.getRole(),
                    expiration
            ));

        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Usuario o contraseña incorrecta");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ocurrió un error inesperado. Intente nuevamente.");
        }
    }

    /**
     * Cierra la sesión del usuario.
     * Invalida la cookie de autenticación estableciendo su tiempo de vida a 0.
     *
     * @param request  Petición HTTP para leer las cookies actuales.
     * @param response Respuesta HTTP para enviar la cookie de borrado.
     * @return Mensaje de éxito o error.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request,
                                    HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("AuthToken".equals(cookie.getName())) {
                    // Crear una cookie con el mismo nombre pero expiración inmediata para borrarla
                    Cookie deleteCookie = new Cookie("AuthToken", "");
                    deleteCookie.setMaxAge(0);
                    deleteCookie.setPath("/");
                    deleteCookie.setHttpOnly(true);
                    response.addCookie(deleteCookie);
                    return ResponseEntity.ok(Map.of("message", "Logout exitoso"));
                }
            }
        }
        return ResponseEntity.ok(Map.of("message", "No fue posible desloguearse"));
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param request Objeto con los datos del nuevo usuario.
     * @return Mensaje de éxito o error si los datos no son válidos.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            ApplicationUser user = userService.registerUser(request);
            return ResponseEntity.ok(Map.of(
                    "message", "Usuario registrado exitosamente",
                    "username", user.getUsername()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
