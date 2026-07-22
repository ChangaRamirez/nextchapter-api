package com.changa.book.domain.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 50)
    private BookProvider provider = BookProvider.MANUAL;

    @Column(name = "external_id", length = 255)
    private String externalId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "isbn", updatable = false, unique = true, length = 17)
    private String isbn;

    @Column(name = "author", nullable = false)
    private String author;

    @ElementCollection
    @CollectionTable(name = "book_genres", joinColumns = @JoinColumn(name = "book_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "genre", nullable = false)
    private Set<BookGenre> genres = new HashSet<>();

    @Column(name = "publication_year", nullable = false)
    private Integer publicationYear;

    @Column(name = "cover_url", length = 2048)
    private String coverUrl;

    @Column(name = "metadata_fetched_at")
    private Instant metadataFetchedAt;

    @Column(name = "created", nullable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", nullable = false)
    private Instant updated;

    public Book() {
    }

    public Book(UUID id, String title, String description, String isbn, String author, Set<BookGenre> genres, Integer publicationYear, Instant created, Instant updated) {
        this.id = id;
        this.provider = BookProvider.MANUAL;
        this.title = title;
        this.description = description;
        this.isbn = isbn;
        this.author = author;
        this.genres = new HashSet<>(genres);
        this.publicationYear = publicationYear;
        this.created = created;
        this.updated = updated;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public BookProvider getProvider() {
        return provider;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Set<BookGenre> getGenres() {
        return genres;
    }

    public void setGenres(Set<BookGenre> genres) {
        this.genres = genres == null
                ? new HashSet<>()
                : new HashSet<>(genres);
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public Instant getMetadataFetchedAt() {
        return metadataFetchedAt;
    }

    public void setMetadataFetchedAt(Instant metadataFetchedAt) {
        this.metadataFetchedAt = metadataFetchedAt;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    public Instant getUpdated() {
        return updated;
    }

    public void setUpdated(Instant updated) {
        this.updated = updated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book book)) return false;

        return id != null && id.equals(book.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", provider=" + provider +
                ", externalId='" + externalId + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", isbn='" + isbn + '\'' +
                ", author='" + author + '\'' +
                ", genres=" + genres +
                ", publicationYear=" + publicationYear +
                ", coverUrl='" + coverUrl + '\'' +
                ", metadataFetchedAt=" + metadataFetchedAt +
                ", created=" + created +
                ", updated=" + updated +
                '}';
    }
}
