package com.changa.reading.statistics.controller;

import com.changa.reading.statistics.domain.dto.StatisticsDto;
import com.changa.reading.statistics.service.StatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/reading/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping()
    ResponseEntity<StatisticsDto> getStatistics() {
        StatisticsDto statisticsDto = statisticsService.getStatistics();

        return ResponseEntity.ok(statisticsDto);
    }
}
