package com.changa.reading.mapper;

import com.changa.reading.domain.CreateReadingEntryRequest;
import com.changa.reading.domain.UpdateReadingEntryReviewRequest;
import com.changa.reading.domain.UpdateReadingEntryRequest;
import com.changa.reading.domain.dto.*;
import com.changa.reading.domain.entity.ReadingEntry;
import com.changa.reading.domain.entity.ReadingNote;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReadingEntryMapper {

    CreateReadingEntryRequest fromDto(CreateReadingEntryRequestDto dto);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "bookId", source = "book.id")
    @Mapping(target = "bookTitle", source = "book.title")
    ReadingEntryDto toDto(ReadingEntry readingEntry);

    ReadingNoteDto toDto(ReadingNote readingNote);

    UpdateReadingEntryRequest fromDto(UpdateReadingEntryRequestDto dto);

    UpdateReadingEntryReviewRequest fromDto(UpdateReadingEntryReviewRequestDto dto);
}
