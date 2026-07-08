package com.changa.exception;

import io.swagger.v3.oas.annotations.media.Schema;

public record ErrorResponseDto(

        @Schema(
                description = "Human-readable error message.",
                example = "Invalid request body."
        )
        String error
) {
}
