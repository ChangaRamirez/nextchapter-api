package com.changa.reading.mapper;

import com.changa.reading.domain.CreateReadingNoteRequest;
import com.changa.reading.domain.UpdateReadingNoteRequest;
import com.changa.reading.domain.dto.CreateReadingNoteRequestDto;
import com.changa.reading.domain.dto.ReadingNoteDto;
import com.changa.reading.domain.dto.UpdateReadingNoteRequestDto;
import com.changa.reading.domain.entity.ReadingNote;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReadingNoteMapper {

    CreateReadingNoteRequest fromDto(CreateReadingNoteRequestDto dto);

    ReadingNoteDto toDto(ReadingNote readingNote);

    UpdateReadingNoteRequest fromDto(UpdateReadingNoteRequestDto dto);
}
