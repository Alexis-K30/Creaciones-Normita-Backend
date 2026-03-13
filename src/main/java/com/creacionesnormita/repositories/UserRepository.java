package com.creacionesnormita.repositories;

import com.creacionesnormita.entity.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Equivale al ApplicationDbContext de .NET pero solo para usuarios.
 * En .NET se usaba _userManager.FindByNameAsync(userName).
 * Aquí se usa userRepository.findByUsername(username).
 */
@Repository
public interface UserRepository extends JpaRepository<ApplicationUser, String> {

    // Equivale a: _userManager.FindByNameAsync(request.UserName)
    Optional<ApplicationUser> findByUsername(String username);

    // Equivale a: _userManager.FindByEmailAsync(email)
    Optional<ApplicationUser> findByEmail(String email);

    // Para validar duplicados en el registro
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
