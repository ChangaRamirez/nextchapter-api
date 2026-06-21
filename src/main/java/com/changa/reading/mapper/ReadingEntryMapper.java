package com.changa.reading.mapper;

import com.changa.reading.domain.CreateReadingEntryRequest;
import com.changa.reading.domain.UpdateReadingEntryRequest;
import com.changa.reading.domain.dto.CreateReadingEntryRequestDto;
import com.changa.reading.domain.dto.ReadingEntryDto;
import com.changa.reading.domain.dto.UpdateReadingEntryRequestDto;
import com.changa.reading.domain.entity.ReadingEntry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReadingEntryMapper {

    CreateReadingEntryRequest fromDto(CreateReadingEntryRequestDto dto);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "bookId", source = "book.id")
    @Mapping(target = "bookTitle", source = "book.title")
    ReadingEntryDto toDto(ReadingEntry readingEntry);

    UpdateReadingEntryRequest fromDto(UpdateReadingEntryRequestDto dto);
}
