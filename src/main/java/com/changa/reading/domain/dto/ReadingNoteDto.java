package com.changa.reading.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

public record ReadingNoteDto(
        @Schema(
                description = "Unique identifier of the reading note.",
                example = "3d9616f5-b0af-4d0a-beab-dee7d7e2960e"
        )
        UUID id,

       @Schema(
               description = "Content of the private reading note.",
               example = "Must finish this book before the end of the summer."
       )
        String content,

        @Schema(
                description = "Timestamp when the reading note was created.",
                example = "2026-07-23T18:30:00Z"
        )
        Instant created,

        @Schema(
                description = "Timestamp when the reading note was last updated.",
                example = "2026-07-23T19:10:00Z"
        )
        Instant updated
) {
}
