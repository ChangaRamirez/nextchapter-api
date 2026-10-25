package com.changa.reading.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record UpdateReadingNoteRequestDto(

        @Schema(
                description = "Content of the private reading note.",
                example = "Must finish this book before the end of the summer."
        )
        @NotBlank(message = ERROR_MESSAGE_CONTENT_REQUIRED)
        @Length(max = 5000, message = ERROR_MESSAGE_CONTENT_LENGTH)
        String content

) {
    private static final String ERROR_MESSAGE_CONTENT_REQUIRED =
            "Content must be provided";
    private static final String ERROR_MESSAGE_CONTENT_LENGTH =
            "Content must be less or equal to 5000 characters";
}
