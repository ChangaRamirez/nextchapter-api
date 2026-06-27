package com.changa.reading.controller;

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
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/reading-entries")
public class ReadingEntryController {

    private final ReadingEntryService readingEntryService;
    private final ReadingEntryMapper readingEntryMapper;

    public ReadingEntryController(ReadingEntryService readingEntryService, ReadingEntryMapper readingEntryMapper) {
        this.readingEntryService = readingEntryService;
        this.readingEntryMapper = readingEntryMapper;
    }

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

    @GetMapping
    public ResponseEntity<Page<ReadingEntryDto>> listReadingEntries(
            @PageableDefault(value = 10, sort = "created")Pageable pageable) {

        Page<ReadingEntry> readingEntries = readingEntryService.listReadingEntries(pageable);

        Page<ReadingEntryDto> readingEntryDto = readingEntries.map(readingEntryMapper::toDto);

        return ResponseEntity.ok(readingEntryDto);
    }

    @GetMapping(path = "/{readingEntryId}")
    public ResponseEntity<ReadingEntryDto> getReadingEntryById(
            @PathVariable("readingEntryId") UUID readingEntryId) {

        ReadingEntry foundReadingEntry = readingEntryService.getReadingEntryById(readingEntryId);

        ReadingEntryDto readingEntryDto = readingEntryMapper.toDto(foundReadingEntry);

        return ResponseEntity.ok(readingEntryDto);
    }

    @GetMapping("/recent")
    public ResponseEntity<Page<ReadingEntryDto>> searchRecentReadingEntries(
            @RequestParam("days") Integer days,@PageableDefault(size = 10, sort = "created") Pageable pageable) {

        Page<ReadingEntry> readingEntries = readingEntryService.searchRecentReadingEntries(days, pageable);

        Page<ReadingEntryDto> readingEntriesDto = readingEntries.map(readingEntryMapper::toDto);

        return ResponseEntity.ok(readingEntriesDto);
    }

    @PutMapping(path = "/{readingEntryId}")
    public ResponseEntity<ReadingEntryDto> updateReadingEntry(
            @PathVariable("readingEntryId") UUID readingEntryId,
            @Valid @RequestBody UpdateReadingEntryRequestDto updateReadingEntryRequestDto) {

        UpdateReadingEntryRequest updateReadingEntryRequest = readingEntryMapper.fromDto(updateReadingEntryRequestDto);

        ReadingEntry updatedReadingEntry = readingEntryService.updateReadingEntry(readingEntryId, updateReadingEntryRequest);

        ReadingEntryDto updatedReadingEntryDto = readingEntryMapper.toDto(updatedReadingEntry);

        return ResponseEntity.ok(updatedReadingEntryDto);
    }

    @PatchMapping(path = "/{readingEntryId}/notes")
    public ResponseEntity<ReadingEntryDto> updateReadingEntryNotes(
            @PathVariable("readingEntryId") UUID readingEntryId,
            @Valid @RequestBody UpdateReadingEntryNotesRequestDto updateReadingEntryNotesRequestDto) {

        UpdateReadingEntryNotesRequest updateReadingEntryNotesRequest = readingEntryMapper.fromDto(updateReadingEntryNotesRequestDto);

        ReadingEntry updatedReadingEntry = readingEntryService.updateReadingEntryNotes(readingEntryId, updateReadingEntryNotesRequest);

        ReadingEntryDto updatedReadingEntryDto = readingEntryMapper.toDto(updatedReadingEntry);

        return ResponseEntity.ok(updatedReadingEntryDto);
    }

    @DeleteMapping(path = "/{readingEntryId}")
    public ResponseEntity<Void> deleteReadingEntry(@PathVariable("readingEntryId") UUID readingEntryId) {
        readingEntryService.deleteReadingEntry(readingEntryId);

        return ResponseEntity.noContent().build();
    }
}
