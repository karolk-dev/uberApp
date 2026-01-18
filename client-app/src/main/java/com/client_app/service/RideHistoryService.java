package com.client_app.service;

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

@Service
@RequiredArgsConstructor
@Slf4j
public class RideHistoryService {

    private final RestTemplate restTemplate;

    @Value("${server.service.rides.url}")
    private String serverRidesUrl;

    public Page<RideHistoryDto> getClientRideHistory(
            String clientUuid,
            RideHistoryFilterDto filterDto,
            int page,
            int size,
            String sort) {

        log.info("Pobieranie historii przejazdów dla klienta: {}", clientUuid);

        // Buduj URI ze wszystkimi parametrami
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverRidesUrl + "/client/" + clientUuid + "/history")
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("sort", sort);

        // Dodaj parametry filtrowania, jeśli są dostępne
        if (filterDto != null) {
            if (filterDto.getStatus() != null) {
                builder.queryParam("status", filterDto.getStatus());
            }
            if (filterDto.getStartDate() != null) {
                builder.queryParam("startDate", filterDto.getStartDate());
            }
            if (filterDto.getEndDate() != null) {
                builder.queryParam("endDate", filterDto.getEndDate());
            }
        }

        String url = builder.toUriString();
        log.debug("URL żądania: {}", url);

        // Użyj RestResponsePage z comm-lib
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
}