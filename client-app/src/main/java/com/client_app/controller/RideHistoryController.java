package com.client_app.controller;

import com.client_app.service.RideHistoryService;
import com.uber.common.dto.RideHistoryDto;
import com.uber.common.dto.RideHistoryFilterDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/client-app/api/rides")
@RequiredArgsConstructor
@Slf4j
public class RideHistoryController {

    private final RideHistoryService rideHistoryService;

    @GetMapping("/client/{clientUuid}/history")
    public ResponseEntity<Page<RideHistoryDto>> getRideHistory(
            @PathVariable String clientUuid,
            RideHistoryFilterDto filterDto,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {


        Page<RideHistoryDto> result = rideHistoryService.getClientRideHistory(
                clientUuid, filterDto, page, size, sort);

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(result.getTotalElements()))
                .header("X-Total-Pages", String.valueOf(result.getTotalPages()))
                .body(result);
    }
}