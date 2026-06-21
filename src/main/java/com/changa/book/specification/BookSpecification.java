package com.changa.book.specification;

import com.changa.book.domain.entity.Book;
import com.changa.book.domain.entity.BookGenre;
import org.springframework.data.jpa.domain.Specification;

public class BookSpecification {

    public static Specification<Book> authorContains(String author) {
        return (root, query, cb) ->
                cb.like(
                        cb.lower(root.get("author")),
                        "%" + author + "%"
                );
    }

    public static Specification<Book> titleContains(String title) {
        return (root, query, cb) ->
                cb.like(
                        cb.lower(root.get("title")),
                        "%" + title.toLowerCase() + "%"
                );
    }

    public static Specification<Book> hasGenre(BookGenre genre) {
        return (root, query, cb) -> {
            query.distinct(true);
            return cb.equal(root.join("genres"), genre);
        };
    }

    public static Specification<Book> hasPublicationYear(Integer publicationYear) {
        return (root, query, cb) ->
                cb.equal(
                        root.get("publicationYear"),
                        publicationYear
                );
    }
}
