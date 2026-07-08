package com.changa.reading.controller;

import com.changa.exception.ErrorResponseDto;
import com.changa.reading.domain.CreateReadingEntryRequest;
import com.changa.reading.domain.UpdateReadingEntryNotesRequest;
import com.changa.reading.domain.UpdateReadingEntryRequest;
import com.changa.reading.domain.dto.CreateReadingEntryRequestDto;
import com.changa.reading.domain.dto.ReadingEntryDto;
import com.changa.reading.domain.dto.UpdateReadingEntryNotesRequestDto;
import com.changa.reading.domain.dto.UpdateReadingEntryRequestDto;
import com.changa.reading.domain.entity.ReadingEntry;
import com.changa.reading.mapper.ReadingEntryMapper;
import com.changa.reading.service.ReadingEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@SecurityRequirement(name = "bearerAuth")
@Tag(
        name = "Reading Entries",
        description = "Endpoints for creating, browsing, searching, updating, and deleting reading entries."
)
@RestController
@RequestMapping(path = "/api/v1/reading-entries")
public class ReadingEntryController {

    private final ReadingEntryService readingEntryService;
    private final ReadingEntryMapper readingEntryMapper;

    public ReadingEntryController(ReadingEntryService readingEntryService, ReadingEntryMapper readingEntryMapper) {
        this.readingEntryService = readingEntryService;
        this.readingEntryMapper = readingEntryMapper;
    }

    @Operation(
            summary = "Create a new reading entry",
            description = "Creates a new reading entry in the authenticated user's personal reading list. A user can only have one reading entry per book. If status is FINISHED, finishedAt is required."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Reading entry created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReadingEntryDto.class)
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
                    description = "Book with the given ID was not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Reading entry with the same book ID already exists",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<ReadingEntryDto> createReadingEntry(
            @Valid @RequestBody CreateReadingEntryRequestDto createReadingEntryRequestDto) {

        CreateReadingEntryRequest readingEntryToCreate = readingEntryMapper.fromDto(createReadingEntryRequestDto);

        ReadingEntry createdReadingEntry = readingEntryService.createReadingEntry(readingEntryToCreate);

        ReadingEntryDto createdReadingEntryDto = readingEntryMapper.toDto(createdReadingEntry);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdReadingEntryDto);
    }

    @Operation(
            summary = "List reading entries",
            description = "Returns a paginated list of reading entries in the authenticated user's personal reading list."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reading entries retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    @Parameters({
            @Parameter(
                    name = "page",
                    description = "Zero-based page index.",
                    example = "0"
            ),
            @Parameter(
                    name = "size",
                    description = "Number of reading entries returned per page.",
                    example = "10"
            ),
            @Parameter(
                    name = "sort",
                    description = "Sorting criteria in the format: property,direction. Example: created,asc",
                    example = "created,asc"
            )
    })
    @GetMapping
    public ResponseEntity<Page<ReadingEntryDto>> listReadingEntries(
            @Parameter(hidden = true)
            @PageableDefault(value = 10, sort = "created") Pageable pageable) {

        Page<ReadingEntry> readingEntries = readingEntryService.listReadingEntries(pageable);

        Page<ReadingEntryDto> readingEntryDto = readingEntries.map(readingEntryMapper::toDto);

        return ResponseEntity.ok(readingEntryDto);
    }

    @Operation(
            summary = "Get reading entry by its ID",
            description = "Retrieves a reading entry using its unique identifier from the authenticated user's personal reading list."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reading entry retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReadingEntryDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid reading entry ID format",
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
                    description = "Reading entry with the given ID was not found in the authenticated user's personal reading list.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    @GetMapping(path = "/{readingEntryId}")
    public ResponseEntity<ReadingEntryDto> getReadingEntryById(
            @Parameter(
                    description = "Reading entry unique identifier.",
                    example = "3d9616f5-b0af-4d0a-beab-dee7d7e2960e"
            )
            @PathVariable("readingEntryId") UUID readingEntryId) {

        ReadingEntry foundReadingEntry = readingEntryService.getReadingEntryById(readingEntryId);

        ReadingEntryDto readingEntryDto = readingEntryMapper.toDto(foundReadingEntry);

        return ResponseEntity.ok(readingEntryDto);
    }

    @Operation(
            summary = "List recent reading entries",
            description = "Returns a paginated list of recent reading entries in the authenticated user's personal reading list."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reading entries retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid days parameter",
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
            )
    })
    @Parameters({
            @Parameter(
                    name = "page",
                    description = "Zero-based page index.",
                    example = "0"
            ),
            @Parameter(
                    name = "size",
                    description = "Number of books returned per page.",
                    example = "10"
            ),
            @Parameter(
                    name = "sort",
                    description = "Sorting criteria in the format: property,direction. Example: created,asc",
                    example = "created,asc"
            )
    })
    @GetMapping("/recent")
    public ResponseEntity<Page<ReadingEntryDto>> searchRecentReadingEntries(
            @Parameter(
                    description = "Number of past days to include in the search.",
                    example = "10"
            )
            @RequestParam("days") Integer days,

            @Parameter(hidden = true)
            @PageableDefault(size = 10, sort = "created") Pageable pageable) {

        Page<ReadingEntry> readingEntries = readingEntryService.searchRecentReadingEntries(days, pageable);

        Page<ReadingEntryDto> readingEntriesDto = readingEntries.map(readingEntryMapper::toDto);

        return ResponseEntity.ok(readingEntriesDto);
    }

    @Operation(
            summary = "Update reading entry",
            description = "Update existing reading entry's status, rating, started at and finished at fields from authenticated user's personal reading list. If rating is provided, status must be FINISHED or ABANDONED. If status is FINISHED, finishedAt is required."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reading entry successfully updated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReadingEntryDto.class)
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
                    description = "Reading entry with the given ID was not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    @PutMapping(path = "/{readingEntryId}")
    public ResponseEntity<ReadingEntryDto> updateReadingEntry(
            @Parameter(
                    description = "Reading entry unique identifier.",
                    example = "3d9616f5-b0af-4d0a-beab-dee7d7e2960e"
            )
            @PathVariable("readingEntryId") UUID readingEntryId,

            @Valid @RequestBody UpdateReadingEntryRequestDto updateReadingEntryRequestDto) {

        UpdateReadingEntryRequest updateReadingEntryRequest = readingEntryMapper.fromDto(updateReadingEntryRequestDto);

        ReadingEntry updatedReadingEntry = readingEntryService.updateReadingEntry(readingEntryId, updateReadingEntryRequest);

        ReadingEntryDto updatedReadingEntryDto = readingEntryMapper.toDto(updatedReadingEntry);

        return ResponseEntity.ok(updatedReadingEntryDto);
    }

    @Operation(
            summary = "Update reading entry's notes and review",
            description = "Update existing reading entry's notes and review fields from authenticated user's personal reading list. If review is provided, status must be FINISHED or ABANDONED."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reading entry successfully updated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReadingEntryDto.class)
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
                    description = "Reading entry with the given ID was not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    @PatchMapping(path = "/{readingEntryId}/notes")
    public ResponseEntity<ReadingEntryDto> updateReadingEntryNotes(
            @Parameter(
                    description = "Reading entry unique identifier.",
                    example = "3d9616f5-b0af-4d0a-beab-dee7d7e2960e"
            )
            @PathVariable("readingEntryId") UUID readingEntryId,

            @Valid @RequestBody UpdateReadingEntryNotesRequestDto updateReadingEntryNotesRequestDto) {

        UpdateReadingEntryNotesRequest updateReadingEntryNotesRequest = readingEntryMapper.fromDto(updateReadingEntryNotesRequestDto);

        ReadingEntry updatedReadingEntry = readingEntryService.updateReadingEntryNotes(readingEntryId, updateReadingEntryNotesRequest);

        ReadingEntryDto updatedReadingEntryDto = readingEntryMapper.toDto(updatedReadingEntry);

        return ResponseEntity.ok(updatedReadingEntryDto);
    }

    @Operation(
            summary = "Delete reading entry",
            description = "Deletes a reading entry from the authenticated user's personal reading list."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Reading entry deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid reading entry ID format",
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
    @DeleteMapping(path = "/{readingEntryId}")
    public ResponseEntity<Void> deleteReadingEntry(
            @Parameter(
                    description = "Reading entry unique identifier.",
                    example = "3d9616f5-b0af-4d0a-beab-dee7d7e2960e"
            )
            @PathVariable("readingEntryId") UUID readingEntryId) {
        readingEntryService.deleteReadingEntry(readingEntryId);

        return ResponseEntity.noContent().build();
    }
}
