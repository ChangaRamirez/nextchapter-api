package com.changa.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record RegisterRequestDto(

        @Schema(
                description = "User's display name.",
                example = "Changa Ramirez"
        )
        @NotBlank(message = ERROR_MESSAGE_NAME_MISSING)
        @Length(max = 255, message = ERROR_MESSAGE_NAME_LENGTH)
        String name,

        @Schema(
                description = "Unique email address used to log in.",
                example = "changa@example.com"
        )
        @NotBlank(message = ERROR_MESSAGE_EMAIL_MISSING)
        @Email(message = ERROR_MESSAGE_EMAIL_INVALID)
        @Length(max = 255, message = ERROR_MESSAGE_EMAIL_LENGTH)
        String email,

        @Schema(
                description = "User password.",
                example = "MySecurePassword123!"
        )
        @NotBlank(message = ERROR_MESSAGE_PASSWORD_MISSING)
        @Length(min = 8, max = 255, message = ERROR_MESSAGE_PASSWORD_LENGTH)
        String password
) {
    private static final String ERROR_MESSAGE_NAME_MISSING =
            "Name must be provided";
    private static final String ERROR_MESSAGE_NAME_LENGTH =
            "Name must be less than 255 characters";
    private static final String ERROR_MESSAGE_EMAIL_MISSING =
            "Email must be provided";
    private static final String ERROR_MESSAGE_EMAIL_INVALID =
            "Email must be valid";
    private static final String ERROR_MESSAGE_EMAIL_LENGTH =
            "Email must be less than 255 characters";
    private static final String ERROR_MESSAGE_PASSWORD_MISSING =
            "Password must be provided";
    private static final String ERROR_MESSAGE_PASSWORD_LENGTH =
            "Password must be between 8 and 255 characters";
}
