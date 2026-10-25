package com.changa.reading.repository;

import com.changa.reading.domain.entity.ReadingNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReadingNoteRepository extends JpaRepository<ReadingNote, UUID> {
}
