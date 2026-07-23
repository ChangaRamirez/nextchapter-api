package com.changa.reading.domain.entity;

import com.changa.book.domain.entity.Book;
import com.changa.user.domain.entity.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "reading_entry")
public class ReadingEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name ="user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReadingStatus status;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "review", length = 2000)
    private String review;

    @OneToMany(mappedBy = "readingEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("created ASC")
    private List<ReadingNote> notes = new ArrayList<>();

    @Column(name = "started_at")
    private LocalDate startedAt;

    @Column(name = "finished_at")
    private LocalDate finishedAt;

    @Column(name = "created", nullable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", nullable = false)
    private Instant updated;

    public ReadingEntry() {
    }

    public ReadingEntry(UUID id, User user, Book book, ReadingStatus status, Integer rating, String review, LocalDate startedAt, LocalDate finishedAt, Instant created, Instant updated) {
        this.id = id;
        this.user = user;
        this.book = book;
        this.status = status;
        this.rating = rating;
        this.review = review;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.created = created;
        this.updated = updated;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Book getBook() {
        return book;
    }

    public ReadingStatus getStatus() {
        return status;
    }

    public void setStatus(ReadingStatus status) {
        this.status = status;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public List<ReadingNote> getNotes() {
        return notes;
    }

    public void addNote(ReadingNote note) {
        notes.add(note);
        note.setReadingEntry(this);
    }

    public void removeNote(ReadingNote note) {
        notes.remove(note);
        note.setReadingEntry(null);
    }

    public LocalDate getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDate startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDate getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDate finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Instant getCreated() {
        return created;
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
        if (!(o instanceof ReadingEntry that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
