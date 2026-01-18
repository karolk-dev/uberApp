package com.driver_app.controller;

import com.driver_app.service.RideHistoryService;
import com.uber.common.dto.RideHistoryDto;
import com.uber.common.dto.RideHistoryFilterDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/driver-app/api/rides")
@RequiredArgsConstructor
@Slf4j
public class RideHistoryController {

    private final RideHistoryService rideHistoryService;

    @GetMapping("/driver/{driverUuid}/history")
    public ResponseEntity<Page<RideHistoryDto>> getRideHistory(
            @PathVariable String driverUuid,
            RideHistoryFilterDto filterDto,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        log.info("Otrzymano żądanie historii przejazdów dla kierowcy: {}", driverUuid);

        Page<RideHistoryDto> result = rideHistoryService.getDriverRideHistory(
                driverUuid, filterDto, page, size, sort);

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(result.getTotalElements()))
                .header("X-Total-Pages", String.valueOf(result.getTotalPages()))
                .body(result);
    }

    @GetMapping("/driver/{driverUuid}/earnings/report")
    public ResponseEntity<byte[]> downloadEarningsReport(
            @PathVariable String driverUuid,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        // Dla braku parametrów, użyj domyślnych wartości
        if (startDate == null) {
            startDate = LocalDateTime.now().minusMonths(1);
        }

        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        byte[] report = rideHistoryService.getEarningsReport(driverUuid, startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("earnings_report_" + driverUuid + ".csv")
                .build());

        return new ResponseEntity<>(report, headers, HttpStatus.OK);
    }
}