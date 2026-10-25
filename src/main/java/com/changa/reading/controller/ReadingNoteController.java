package com.changa.reading.controller;

import com.changa.exception.ErrorResponseDto;
import com.changa.reading.domain.CreateReadingNoteRequest;
import com.changa.reading.domain.UpdateReadingNoteRequest;
import com.changa.reading.domain.dto.CreateReadingNoteRequestDto;
import com.changa.reading.domain.dto.ReadingNoteDto;
import com.changa.reading.domain.dto.UpdateReadingNoteRequestDto;
import com.changa.reading.domain.entity.ReadingNote;
import com.changa.reading.mapper.ReadingNoteMapper;
import com.changa.reading.service.ReadingNoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Reading Notes", description = "Private notes managed through the authenticated user's reading entries.")
@RestController
@RequestMapping("/api/v1/reading-entries/{readingEntryId}/notes")
public class ReadingNoteController {

    private final ReadingNoteService readingNoteService;
    private final ReadingNoteMapper readingNoteMapper;

    public ReadingNoteController(ReadingNoteService readingNoteService, ReadingNoteMapper readingNoteMapper) {
        this.readingNoteService = readingNoteService;
        this.readingNoteMapper = readingNoteMapper;
    }

    @Operation(
            summary = "Create a new reading note",
            description = "Creates a new private note for one of the authenticated user's reading entries. A user can only create and access notes from within a reading entry. A reading entry can have multiple notes."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Reading note created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReadingNoteDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reading entry with the given ID was not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<ReadingNoteDto> createReadingNote(
            @Parameter(
                    description = "Reading entry unique identifier.",
                    example = "3d9616f5-b0af-4d0a-beab-dee7d7e2960e"
            )
            @PathVariable("readingEntryId") UUID readingEntryId,
            @Valid @RequestBody CreateReadingNoteRequestDto createReadingNoteRequestDto) {

        CreateReadingNoteRequest readingNoteToCreate = readingNoteMapper.fromDto(createReadingNoteRequestDto);

        ReadingNote createdReadingNote = readingNoteService.createReadingNote(readingEntryId, readingNoteToCreate);

        ReadingNoteDto createdReadingNoteDto = readingNoteMapper.toDto(createdReadingNote);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdReadingNoteDto);
    }

    @Operation(
            summary = "Update reading note",
            description = "Update existing reading note's body from authenticated user's personal reading list's entry."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reading note successfully updated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReadingNoteDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reading entry or reading note was not found.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    @PutMapping(path = "/{readingNoteId}")
    public ResponseEntity<ReadingNoteDto> updateReadingNote(
            @Parameter(
                    description = "Reading entry unique identifier.",
                    example = "3d9616f5-b0af-4d0a-beab-dee7d7e2960e"
            )
            @PathVariable("readingEntryId") UUID readingEntryId,
            @Parameter(
                    description = "Reading note unique identifier.",
                    example = "0e725163-d734-45cb-b642-2993dd37b120"
            )
            @PathVariable("readingNoteId") UUID readingNoteId,
            @Valid @RequestBody UpdateReadingNoteRequestDto updateReadingNoteRequestDto) {

        UpdateReadingNoteRequest readingNoteToUpdate = readingNoteMapper.fromDto(updateReadingNoteRequestDto);

        ReadingNote updatedReadingNote = readingNoteService.updateReadingNote(readingEntryId, readingNoteId, readingNoteToUpdate);

        ReadingNoteDto updatedReadingNoteDto = readingNoteMapper.toDto(updatedReadingNote);

        return ResponseEntity
                .ok(updatedReadingNoteDto);
    }

    @Operation(
            summary = "Delete reading note",
            description = "Deletes a reading note from the authenticated user's personal reading list's entry."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Reading note deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid reading entry or reading note ID format",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reading entry or reading note was not found.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    @DeleteMapping(path = "/{readingNoteId}")
    public ResponseEntity<Void> deleteReadingNote(
            @Parameter(
                    description = "Reading entry unique identifier.",
                    example = "3d9616f5-b0af-4d0a-beab-dee7d7e2960e"
            )
            @PathVariable("readingEntryId") UUID readingEntryId,
            @Parameter(
                    description = "Reading note unique identifier.",
                    example = "0e725163-d734-45cb-b642-2993dd37b120"
            )
            @PathVariable("readingNoteId") UUID readingNoteId) {

        readingNoteService.deleteReadingNote(readingEntryId, readingNoteId);

        return ResponseEntity.noContent().build();
    }

}
