package com.changa.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record AuthResponseDto(

        @Schema(
                description = "JWT token used to authenticate protected requests.",
                example = "eyJhbGciOiJIUzI1NiJ9..."
        )
        String token,

        @Schema(
                description = "Authenticated user's unique identifier.",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
        )
        UUID userId,

        @Schema(
                description = "Authenticated user's display name",
                example = "Changa Ramirez"
        )
        String name,

        @Schema(
                description = "Authenticated user's email address.",
                example = "changa@example.com"
        )
        String email
) {}
