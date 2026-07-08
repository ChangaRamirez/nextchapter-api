package com.changa.reading.statistics.controller;

import com.changa.exception.ErrorResponseDto;
import com.changa.reading.statistics.domain.dto.StatisticsDto;
import com.changa.reading.statistics.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = "bearerAuth")
@Tag(
        name = "Statistics",
        description = "Endpoints for retrieving personal reading statistics."
)
@RestController
@RequestMapping(path = "/api/v1/reading/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @Operation(
            summary = "Get reading statistics",
            description = "Returns reading statistics for the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Statistics retrieves successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StatisticsDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    @GetMapping()
    public ResponseEntity<StatisticsDto> getStatistics() {
        StatisticsDto statisticsDto = statisticsService.getStatistics();

        return ResponseEntity.ok(statisticsDto);
    }
}
