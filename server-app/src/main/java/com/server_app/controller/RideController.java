package com.server_app.controller;

import com.itextpdf.text.DocumentException;
import com.server_app.config.RideHistoryMapper;
import com.server_app.dto.RideHistorySearchCriteria;
import com.server_app.exceptions.InvoiceProcessingException;
import com.server_app.model.command.EditRideCommand;
import com.server_app.service.DriverEarningsService;
import com.server_app.service.EmailService;
import com.server_app.service.RideRequestInfo;
import com.server_app.service.RideService;
import com.uber.common.Coordinates;
import com.uber.common.dto.RideHistoryDto;
import com.uber.common.dto.RideHistoryFilterDto;
import com.uber.common.model.RideStatus;
import com.uber.common.productSelector.RideDataInfoDto;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
@Slf4j
public class RideController {

    private final RideService rideService;
    private final RideRequestInfo rideRequestInfo;
    private final RideHistoryMapper rideHistoryMapper;
    private final EmailService emailService;
    private final DriverEarningsService driverEarningsService;
//    private final RestTemplate restTemplate;
//    private final String clientServiceUrl;


    @GetMapping("/info")
    public ResponseEntity<RideDataInfoDto> getRideInfo(
            @RequestParam("pickupLatitude") double pickupLatitude,
            @RequestParam("pickupLongitude") double pickupLongitude,
            @RequestParam("destinationLatitude") double destinationLatitude,
            @RequestParam("destinationLongitude") double destinationLongitude) throws Exception {

        // Utworzenie obiektów Coordinates na podstawie przekazanych wartości
        Coordinates pickupLocation = new Coordinates(pickupLatitude, pickupLongitude);
        Coordinates destinationLocation = new Coordinates(destinationLatitude, destinationLongitude);

        RideDataInfoDto rideDataInfoDto = rideRequestInfo.processRideRequest(pickupLocation, destinationLocation);
        return ResponseEntity.ok(rideDataInfoDto);
    }

    @PostMapping("/penalty/{rideUuid}")
    public ResponseEntity<?> penalty(@PathVariable String rideUuid) {
        EditRideCommand editRideCommand = EditRideCommand.builder()
                .penaltyAmount(30000)
                .build();
        return ResponseEntity.ok(rideService.editRide(editRideCommand, rideUuid));
    }

    @PostMapping("/finish/{rideUuid}")
    public ResponseEntity<RideStatus> finishRide(@PathVariable String rideUuid) throws Exception {
        return ResponseEntity.ok(rideService.finishRide(rideUuid).getStatus());
    }

    @PostMapping("invoice")
    public ResponseEntity<?> sendInvoice(@RequestParam("email") String email) {
        try {
            emailService.sendInvoiceEmail(email, null);
            return ResponseEntity.ok("Faktura została wysłana na adres: " + email);
        } catch (DocumentException | MessagingException | IOException e) {
            throw new InvoiceProcessingException("Błąd podczas wysyłania faktury na adres: " + email, e);
        } catch (Exception e) {
            throw new InvoiceProcessingException("Nieoczekiwany błąd podczas wysyłania faktury na adres: " + email, e);
        }
    }

    @GetMapping("/driver/{driverUuid}/history")
    public ResponseEntity<Page<RideHistoryDto>> getDriverRideHistory(
            @PathVariable String driverUuid,
            RideHistoryFilterDto filterDto,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        log.info("Otrzymano zapytanie");
        // Walidacja parametrów sortowania
        Sort.Direction direction = Sort.Direction.DESC;
        String sortField = "createdAt";

        if (sort != null && !sort.isEmpty()) {
            String[] sortParams = sort.split(",");
            if (sortParams.length > 1) {
                sortField = sortParams[0];
                direction = Sort.Direction.fromString(sortParams[1].toUpperCase());
            }
        }

        // Tworzenie PageRequest z parametrami sortowania
        PageRequest pageRequest = PageRequest.of(page, size, direction, sortField);

        // Konwersja filterDto na SearchCriteria
        RideHistorySearchCriteria searchCriteria = rideHistoryMapper.toSearchCriteria(filterDto);

        // Wywołanie serwisu i zwrócenie odpowiedzi
        Page<RideHistoryDto> result = rideService.getDriverRideHistory(
                driverUuid,
                searchCriteria,
                pageRequest
        );

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(result.getTotalElements()))
                .header("X-Total-Pages", String.valueOf(result.getTotalPages()))
                .body(result);
    }


    @GetMapping("/driver/{driverUuid}/earnings/report")
    public ResponseEntity<byte[]> downloadEarningsReport(
            @PathVariable String driverUuid,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        byte[] report = driverEarningsService.generateEarningsReport(driverUuid, startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("earnings_report_" + driverUuid + ".csv")
                .build());

        return new ResponseEntity<>(report, headers, HttpStatus.OK);
    }

    @GetMapping("/client/{clientUuid}/history")
    public ResponseEntity<Page<RideHistoryDto>> getClientRideHistory(
            @PathVariable String clientUuid,
            RideHistoryFilterDto filterDto,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        log.info("Otrzymano zapytanie o historię przejazdów klienta");

        // Walidacja parametrów sortowania
        Sort.Direction direction = Sort.Direction.DESC;
        String sortField = "createdAt";

        if (sort != null && !sort.isEmpty()) {
            String[] sortParams = sort.split(",");
            if (sortParams.length > 1) {
                sortField = sortParams[0];
                direction = Sort.Direction.fromString(sortParams[1].toUpperCase());
            }
        }

        // Tworzenie PageRequest z parametrami sortowania
        PageRequest pageRequest = PageRequest.of(page, size, direction, sortField);

        // Konwersja filterDto na SearchCriteria
        RideHistorySearchCriteria searchCriteria = rideHistoryMapper.toSearchCriteria(filterDto);

        // Wywołanie serwisu i zwrócenie odpowiedzi
        Page<RideHistoryDto> result = rideService.getClientRideHistory(
                clientUuid,
                searchCriteria,
                pageRequest
        );

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(result.getTotalElements()))
                .header("X-Total-Pages", String.valueOf(result.getTotalPages()))
                .body(result);
    }
}