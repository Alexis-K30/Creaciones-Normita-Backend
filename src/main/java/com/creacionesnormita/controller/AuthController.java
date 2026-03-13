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
 * Equivale a LoginController.cs + RegisterController.cs del proyecto .NET
 *
 * .NET:  [Route("api/[controller]")] → /api/Login, /api/Register
 * Spring: [RequestMapping("/api/auth")] → /api/auth/login, /api/auth/register
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    // ─────────────────────────────────────────────────────────────────────────────
    // POST /api/auth/login
    // Equivale a: LoginController.Login([FromBody] LoginRequest request)
    // ─────────────────────────────────────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request,
                                   HttpServletResponse response) {
        try {
            // Equivale a: _userManager.FindByNameAsync(request.UserName)
            ApplicationUser user = (ApplicationUser)
                    userService.loadUserByUsername(request.username());

            // Equivale a: _passwordHasher.VerifyHashedPassword(user, user.PasswordHash, request.Password)
            if (!passwordEncoder.matches(request.password(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Usuario o contraseña incorrecta");
            }

            // Equivale a: _jwtService.GenerateToken(user, roles, out DateTime? expires)
            String token = jwtService.generateToken(user);
            var expiration = jwtService.getExpiration(token);

            // Equivale a: Response.Cookies.Append("AuthToken", jwtString, cookieOptions)
            //   HttpOnly = true
            //   Secure   = true
            //   SameSite = Lax
            Cookie cookie = new Cookie("AuthToken", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(false); // Cambiar a true en producción con HTTPS
            cookie.setPath("/");
            cookie.setMaxAge(
                "Administrador".equalsIgnoreCase(user.getRole())
                    ? 365 * 24 * 60 * 60   // 1 año para admins
                    : 60 * 60              // 1 hora para usuarios normales
            );
            response.addCookie(cookie);

            // Equivale a: return Ok(new LoginResponse { Token = ..., Expiration = ... })
            return ResponseEntity.ok(new LoginResponse(
                    token,
                    user.getUsername(),
                    user.getRole(),
                    expiration
            ));

        } catch (UsernameNotFoundException e) {
            // Equivale a: return Unauthorized("Usuario o contraseña incorrecta")
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Usuario o contraseña incorrecta");
        } catch (Exception e) {
            // Equivale a: return StatusCode(500, "Ocurrió un error inesperado...")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ocurrió un error inesperado. Intente nuevamente.");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // POST /api/auth/logout
    // Equivale a: LoginController.Logout()
    // ─────────────────────────────────────────────────────────────────────────────
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request,
                                    HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("AuthToken".equals(cookie.getName())) {
                    // Equivale a: Expires = DateTime.UtcNow.AddDays(-1)
                    Cookie deleteCookie = new Cookie("AuthToken", "");
                    deleteCookie.setMaxAge(0);   // Expira inmediatamente
                    deleteCookie.setPath("/");
                    deleteCookie.setHttpOnly(true);
                    response.addCookie(deleteCookie);
                    return ResponseEntity.ok(Map.of("message", "Logout exitoso"));
                }
            }
        }
        return ResponseEntity.ok(Map.of("message", "No fue posible desloguearse"));
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // POST /api/auth/register
    // Equivale a: RegisterController.Register([FromBody] RegisterRequest request)
    // ─────────────────────────────────────────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            // Equivale a: await _registrationService.RegisterUserAsync(request, User)
            ApplicationUser user = userService.registerUser(request);
            return ResponseEntity.ok(Map.of(
                    "message", "Usuario registrado exitosamente",
                    "username", user.getUsername()
            ));
        } catch (RuntimeException e) {
            // Equivale a: return BadRequest(result)
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
