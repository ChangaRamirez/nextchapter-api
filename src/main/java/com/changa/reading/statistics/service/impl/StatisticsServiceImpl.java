package com.changa.reading.statistics.service.impl;

import com.changa.reading.domain.entity.ReadingStatus;
import com.changa.reading.repository.ReadingEntryRepository;
import com.changa.reading.statistics.domain.dto.StatisticsDto;
import com.changa.reading.statistics.service.StatisticsService;
import org.springframework.stereotype.Service;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final ReadingEntryRepository readingEntryRepository;

    public StatisticsServiceImpl(ReadingEntryRepository readingEntryRepository) {
        this.readingEntryRepository = readingEntryRepository;
    }

    @Override
    public StatisticsDto getStatistics() {
        return new StatisticsDto(
                readingEntryRepository.count(),
                readingEntryRepository.countByStatus(ReadingStatus.FINISHED),
                readingEntryRepository.countByStatus(ReadingStatus.READING),
                readingEntryRepository.countByStatus(ReadingStatus.TO_READ),
                readingEntryRepository.findAverageRating()
        );
    }
}
