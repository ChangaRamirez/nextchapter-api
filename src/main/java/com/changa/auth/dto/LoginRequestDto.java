package com.changa.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record LoginRequestDto(

        @NotBlank(message = ERROR_MESSAGE_EMAIL_MISSING)
        @Email(message = ERROR_MESSAGE_EMAIL_INVALID)
        @Length(max = 255, message = ERROR_MESSAGE_EMAIL_LENGTH)
        String email,

        @NotBlank(message = ERROR_MESSAGE_PASSWORD_MISSING)
        String password
) {
    private static final String ERROR_MESSAGE_EMAIL_MISSING =
            "Email must be provided";
    private static final String ERROR_MESSAGE_EMAIL_INVALID =
            "Email must be valid";
    private static final String ERROR_MESSAGE_EMAIL_LENGTH =
            "Email must be less than 255 characters";
    private static final String ERROR_MESSAGE_PASSWORD_MISSING =
            "Password must be provided";
}
