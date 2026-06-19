package com.changa.book.mapper;

import com.changa.book.domain.CreateBookRequest;
import com.changa.book.domain.UpdateBookRequest;
import com.changa.book.domain.dto.BookDto;
import com.changa.book.domain.dto.CreateBookRequestDto;
import com.changa.book.domain.dto.UpdateBookRequestDto;
import com.changa.book.domain.entity.Book;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface BookMapper {

    CreateBookRequest fromDto(CreateBookRequestDto dto);

    BookDto toDto(Book book);

    UpdateBookRequest fromDto(UpdateBookRequestDto dto);
}
