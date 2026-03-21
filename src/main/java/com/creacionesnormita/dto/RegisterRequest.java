package com.creacionesnormita.dto;

import java.time.LocalDate;

/**
 * Equivale a RegisterRequest en Shared.Models del proyecto .NET
 */
public record RegisterRequest(
        String username,
        String email,
        String password,
        String nombre,
        String pais
) {}
