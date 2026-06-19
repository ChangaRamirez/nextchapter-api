package com.changa.reading.repository;

import com.changa.reading.domain.entity.ReadingEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.UUID;

public interface ReadingEntryRepository extends JpaRepository<ReadingEntry, UUID> {

    Page<ReadingEntry> findByCreatedGreaterThanEqual(Instant createdAfter, Pageable pageable);

    boolean existsByBook_Id(UUID id);
}
