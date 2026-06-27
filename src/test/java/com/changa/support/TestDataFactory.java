package com.changa.support;

import com.changa.book.domain.entity.Book;
import com.changa.book.domain.entity.BookGenre;
import com.changa.reading.domain.entity.ReadingEntry;
import com.changa.reading.domain.entity.ReadingStatus;
import com.changa.user.domain.entity.User;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public final class TestDataFactory {

    private TestDataFactory() {}

    public static User createUser() {
        return new User(
                UUID.fromString("0e725163-d734-45cb-b642-2993dd37b120"),
                "eduardo@test.com",
                "encoded-password",
                "Eduardo",
                Instant.now(),
                Instant.now()
        );
    }

    public static Book createBook() {
        return new Book(
                UUID.fromString("a3c862a1-ca6f-48bb-8d2f-1ca6d4357f9c"),
                "Dune",
                "A sci-fi classic",
                "978-0441172719",
                "Frank Herbert",
                Set.of(
                        BookGenre.SCIENCE_FICTION,
                        BookGenre.ADVENTURE
                ),
                1965,
                Instant.now(),
                Instant.now()
        );
    }

    public static ReadingEntry createReadingEntry(User user, Book book) {
        return new ReadingEntry(
                UUID.fromString("b82780fe-c934-40e1-887e-9a0afa0dce91"),
                user,
                book,
                ReadingStatus.FINISHED,
                6,
                null,
                null,
                LocalDate.parse("2025-01-15"),
                LocalDate.parse("2025-02-01"),
                Instant.now(),
                Instant.now()
        );
    }
}
