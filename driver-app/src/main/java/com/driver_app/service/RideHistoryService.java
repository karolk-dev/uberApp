package com.driver_app.service;

import com.uber.common.dto.RestResponsePage;
import com.uber.common.dto.RideHistoryDto;
import com.uber.common.dto.RideHistoryFilterDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class RideHistoryService {

    private final RestTemplate restTemplate;

    @Value("${server.service.rides.url}")
    private String serverRidesUrl;

    public Page<RideHistoryDto> getDriverRideHistory(
            String driverUuid,
            RideHistoryFilterDto filterDto,
            int page,
            int size,
            String sort) {

        log.info("Pobieranie historii przejazdów dla kierowcy: {}", driverUuid);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(serverRidesUrl + "/driver/" + driverUuid + "/history")
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("sort", sort);

        if (filterDto.getStatus() != null) {
            uriBuilder.queryParam("status", filterDto.getStatus());
        }

        if (filterDto.getStartDate() != null) {
            uriBuilder.queryParam("startDate", filterDto.getStartDate().format(DateTimeFormatter.ISO_DATE_TIME));
        }

        if (filterDto.getEndDate() != null) {
            uriBuilder.queryParam("endDate", filterDto.getEndDate().format(DateTimeFormatter.ISO_DATE_TIME));
        }

        String url = uriBuilder.toUriString();
        log.debug("URL żądania: {}", url);

        ParameterizedTypeReference<RestResponsePage<RideHistoryDto>> responseType =
                new ParameterizedTypeReference<>() {
                };

        ResponseEntity<RestResponsePage<RideHistoryDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                responseType
        );

        return response.getBody();
    }

    public byte[] getEarningsReport(
            String driverUuid,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        log.info("Pobieranie raportu zarobków dla kierowcy: {}", driverUuid);

        String url = UriComponentsBuilder.fromHttpUrl(serverRidesUrl + "/driver/" + driverUuid + "/earnings/report")
                .queryParam("startDate", startDate)
                .queryParam("endDate", endDate)
                .toUriString();

        log.debug("URL żądania: {}", url);

        ResponseEntity<byte[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                byte[].class
        );

        return response.getBody();
    }
}