package com.changa.book.repository;

import com.changa.book.domain.entity.Book;
import com.changa.book.domain.entity.BookGenre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {

    Optional<Book> findByIsbn(String isbn);

    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Book> findByAuthorContainingIgnoreCase(String author, Pageable pageable);

    Page<Book> findByGenresContaining(BookGenre genre, Pageable pageable);

    @Query("SELECT DISTINCT b.author FROM Book b ORDER BY b.author ASC")
    List<String> findDistinctAuthors();

    Page<Book> findByPublicationYearBetween(Integer startYear, Integer endYear, Pageable pageable);

    boolean existsByIsbn(String isbn);
}
