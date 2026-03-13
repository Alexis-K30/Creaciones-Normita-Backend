package com.creacionesnormita.dto;

import java.time.Instant;

/**
 * Equivale a LoginResponse en Shared.Models del proyecto .NET
 */
public record LoginResponse(String token, String username, String role, Instant expiration) {}
