package com.changa.reading.repository;

import com.changa.reading.domain.entity.ReadingEntry;
import com.changa.reading.domain.entity.ReadingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.UUID;

public interface ReadingEntryRepository extends JpaRepository<ReadingEntry, UUID> {

    Page<ReadingEntry> findByCreatedGreaterThanEqual(Instant createdAfter, Pageable pageable);

    boolean existsByBook_Id(UUID id);

    boolean existsByUser_IdAndBook_Id(UUID userId, UUID bookId);

    long countByStatus(ReadingStatus status);

    @Query("""
            SELECT AVG(r.rating)
            FROM ReadingEntry r
            WHERE r.rating IS NOT NULL
            """)
    Double findAverageRating();
}
