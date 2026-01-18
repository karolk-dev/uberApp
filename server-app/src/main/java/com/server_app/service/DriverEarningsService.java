package com.server_app.service;

import com.opencsv.CSVWriter;
import com.server_app.dto.EarningsSummary;
import com.server_app.dto.RideHistorySearchCriteria;
import com.uber.common.dto.RideHistoryDto;
import com.uber.common.model.RideStatus;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverEarningsService {
    private final RideService rideService;
    private final EmailService emailService;

    public byte[] generateEarningsReport(String driverUuid, LocalDateTime startDate, LocalDateTime endDate) {
        RideHistorySearchCriteria searchCriteria = RideHistorySearchCriteria.builder()
                .startDate(startDate)
                .endDate(endDate)
                .status(RideStatus.COMPLETED)
                .isPaid(true)
                .build();

        PageRequest pageRequest = PageRequest.of(0, Integer.MAX_VALUE);
        Page<RideHistoryDto> rides = rideService.getDriverRideHistory(driverUuid, searchCriteria, pageRequest);

        return generateCsvFile(rides.getContent());
    }

    private byte[] generateCsvFile(List<RideHistoryDto> rides) {
        StringWriter stringWriter = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(stringWriter);


        String[] headers = {
                "Date of ride",
                "Pickup location",
                "Destination location",
                "Fare amount",
                "Curreny",
                "Payment status",
                "Payment type",
                "Ride ID"
        };
        csvWriter.writeNext(headers);

        // Dane
        rides.forEach(ride -> {
            String[] line = {
                    ride.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    ride.getPickupLocationLatitude().toString(),
                    ride.getPickupLocationLongitude().toString(),
                    ride.getDestinationLatitude().toString(),
                    ride.getDestinationLongitude().toString(),
                    ride.getAmount().toString(),
                    ride.getCurrency(),
                    ride.isPaid() ? "Opłacony" : "Nieopłacony",
                    ride.getPaymentType().toString(),
                    ride.getRideUuid()
            };
            csvWriter.writeNext(line);
        });

        return stringWriter.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Async
    public CompletableFuture<Void> generateAndSendEarningsReport(
            String driverUuid,
            String driverEmail,
            LocalDateTime startDate,
            LocalDateTime endDate) throws MessagingException {

        byte[] report = generateEarningsReport(driverUuid, startDate, endDate);

        emailService.sendEarningsReport(driverEmail, report);

        return CompletableFuture.completedFuture(null);
    }

    public EarningsSummary calculateEarningsSummary(List<RideHistoryDto> rides) {
        BigDecimal totalEarnings = rides.stream()
                .map(RideHistoryDto::getAmount)  // Pamiętaj o zmianie na getFareAmount
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> earningsByPaymentType = rides.stream()
                .collect(Collectors.groupingBy(
                        ride -> ride.getPaymentType().toString(),
                        Collectors.reducing(BigDecimal.ZERO,
                                RideHistoryDto::getAmount,  // Pamiętaj o zmianie na getFareAmount
                                BigDecimal::add)
                ));

        return EarningsSummary.builder()
                .totalEarnings(totalEarnings)
                .totalRides(rides.size())
                .earningsByPaymentType(earningsByPaymentType)
                .averagePerRide(rides.isEmpty() ?
                        BigDecimal.ZERO :
                        totalEarnings.divide(BigDecimal.valueOf(rides.size()), 2, RoundingMode.HALF_UP))
                .build();
    }
}