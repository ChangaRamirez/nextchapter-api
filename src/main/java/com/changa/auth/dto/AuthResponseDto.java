package com.changa.auth.dto;

import java.util.UUID;

public record AuthResponseDto(
        String token,
        UUID userId,
        String name,
        String email
) {}
