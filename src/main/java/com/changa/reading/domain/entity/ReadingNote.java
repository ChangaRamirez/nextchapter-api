package com.changa.reading.domain.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reading_note")
public class ReadingNote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "content", nullable = false, length = 5000)
    private String content;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reading_entry_id", nullable = false)
    private ReadingEntry readingEntry;

    @Column(name = "created", nullable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", nullable = false)
    private Instant updated;

    public ReadingNote() {
    }

    public ReadingNote(String content, Instant created, Instant updated) {
        this.content = content;
        this.created = created;
        this.updated = updated;
    }

    public UUID getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ReadingEntry getReadingEntry() {
        return readingEntry;
    }

    public void setReadingEntry(ReadingEntry readingEntry) {
        this.readingEntry = readingEntry;
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
        if (!(o instanceof ReadingNote that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
