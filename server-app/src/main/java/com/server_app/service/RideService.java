package com.server_app.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.server_app.dto.RideHistorySearchCriteria;
import com.server_app.exceptions.RideNotFoundException;
import com.server_app.model.QRide;
import com.server_app.model.Ride;
import com.server_app.model.command.EditRideCommand;
import com.server_app.repository.RideRepository;
import com.uber.common.dto.RideHistoryDto;
import com.uber.common.model.RideStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RideService {
    private final RideRepository rideRepository;
    private final ModelMapper modelMapper;
    private final PaymentService paymentService;
    private final EmailService emailService;
    @PersistenceContext
    private EntityManager entityManager;

    public RideStatus getRideStatus(Long rideId) {
        return rideRepository.findById(rideId)
                .map(Ride::getStatus)
                .orElse(null);
    }

    public void save(Ride ride) {
        rideRepository.save(ride);
    }

    @Transactional
    public Ride editRide(EditRideCommand command, String uuid) {
        return rideRepository.findByUuid(uuid).map(ride -> {
                    Optional.ofNullable(command.getClientUuid()).ifPresent(ride::setClientUuid);
                    Optional.ofNullable(command.getUuid()).ifPresent(ride::setUuid);
                    Optional.ofNullable(command.getDriverUuid()).ifPresent(ride::setDriverUuid);
                    Optional.ofNullable(command.getPickupLocationLatitude()).ifPresent(ride::setPickupLocationLatitude);
                    Optional.ofNullable(command.getPickupLocationLongitude()).ifPresent(ride::setPickupLocationLongitude);
                    Optional.ofNullable(command.getDestinationLatitude()).ifPresent(ride::setDestinationLatitude);
                    Optional.ofNullable(command.getDestinationLongitude()).ifPresent(ride::setDestinationLongitude);
                    Optional.ofNullable(command.getUpdatedAt()).ifPresent(ride::setUpdatedAt);
                    Optional.ofNullable(command.getStatus()).ifPresent(ride::setStatus);
                    Optional.of(command.getPenaltyAmount()).ifPresent(ride::setPenaltyAmount);

                    ride.setUpdatedAt(LocalDateTime.now());
                    return ride;
                })
                .orElseThrow(() -> new RideNotFoundException("Ride not found with UUID: " + uuid));
    }

    @Transactional
    public Ride finishRide(String rideUuid) throws Exception {
        Ride ride = rideRepository.findByUuid(rideUuid).orElseThrow(() -> new RuntimeException("ride not Found"));
        paymentService.updatePaymentIntent(ride.getPaymentIntendId(), ride.getAmount() + ride.getPenaltyAmount());
        paymentService.capturePayment(ride.getPaymentIntendId());
        emailService.sendInvoiceEmail("karkac9067@gmail.com", ride);
        ride.setStatus(RideStatus.COMPLETED);
        rideRepository.save(ride);
        return ride;
    }

    @Transactional(readOnly = true)
    public Page<RideHistoryDto> getDriverRideHistory(
            String driverUuid,
            RideHistorySearchCriteria searchCriteria,
            PageRequest pageRequest) {

        log.info("Szukanie historii");

        // Tworzenie predykatu dla QueryDSL
        QRide ride = QRide.ride;
        BooleanBuilder predicate = new BooleanBuilder();

        // Podstawowy warunek - przejazdy dla danego kierowcy
        predicate.and(ride.driverUuid.eq(driverUuid));

        // Dodawanie warunków na podstawie kryteriów wyszukiwania
        if (searchCriteria.getStartDate() != null) {
            predicate.and(ride.createdAt.goe(searchCriteria.getStartDate()));
        }

        if (searchCriteria.getEndDate() != null) {
            predicate.and(ride.createdAt.loe(searchCriteria.getEndDate()));
        }

        if (searchCriteria.getStatus() != null) {
            predicate.and(ride.status.eq(searchCriteria.getStatus()));
        }

        if (searchCriteria.getIsPaid() != null) {
            predicate.and(ride.isPaid.eq(searchCriteria.getIsPaid()));
        }

        if (searchCriteria.getPaymentType() != null) {
            predicate.and(ride.paymentType.eq(searchCriteria.getPaymentType()));
        }

        // Wykonanie zapytania z użyciem QueryDSL
        JPAQuery<RideHistoryDto> query = new JPAQueryFactory(entityManager)
                .select(Projections.bean(RideHistoryDto.class,
                        ride.uuid.as("rideUuid"),
                        ride.createdAt,
                        ride.pickupLocationLongitude,
                        ride.pickupLocationLatitude,
                        ride.destinationLongitude,
                        ride.destinationLatitude,
                        ride.status,
                        ride.amount,
                        ride.currency,
                        ride.isPaid,
                        ride.paymentType
//                        ride.clientName
                ))
                .from(ride)
                .where(predicate)
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize());

        List<RideHistoryDto> rides = query.fetch();
        long total = query.fetchCount();

        return new PageImpl<>(rides, pageRequest, total);
    }

    @Transactional(readOnly = true)
    public Page<RideHistoryDto> getClientRideHistory(
            String clientUuid,
            RideHistorySearchCriteria searchCriteria,
            PageRequest pageRequest) {

        QRide ride = QRide.ride;

        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(ride.clientUuid.eq(clientUuid));

        // Dodawanie filtrów
        if (searchCriteria.getStartDate() != null) {
            predicate.and(ride.createdAt.goe(searchCriteria.getStartDate()));
        }

        if (searchCriteria.getEndDate() != null) {
            predicate.and(ride.createdAt.loe(searchCriteria.getEndDate()));
        }

        // Wykonanie zapytania z użyciem QueryDSL
        JPAQuery<RideHistoryDto> query = new JPAQueryFactory(entityManager)
                .select(Projections.bean(RideHistoryDto.class,
                        ride.uuid.as("rideUuid"),
                        ride.createdAt,
                        ride.pickupLocationLongitude,
                        ride.pickupLocationLatitude,
                        ride.destinationLongitude,
                        ride.destinationLatitude,
                        ride.status,
                        ride.amount,
                        ride.currency,
                        ride.isPaid,
                        ride.paymentType
//                        ride.driverName
                ))
                .from(ride)
                .where(predicate)
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize());

        List<RideHistoryDto> rides = query.fetch();
        long total = query.fetchCount();

        return new PageImpl<>(rides, pageRequest, total);
    }

    private String formatLocation(Double latitude, Double longitude) {
        return String.format("%.6f, %.6f", latitude, longitude);
    }

    public Optional<Ride> findByUuid(String uuid) {
        return rideRepository.findByUuid(uuid);
    }
}
