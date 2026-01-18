package com.server_app.repository;

import com.server_app.model.Ride;
import com.uber.common.model.RideStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RideRepository extends JpaRepository<Ride, Long>, QuerydslPredicateExecutor<Ride> {
    Optional<Ride> findById(Long id);
    Optional<Ride> findByUuid(String uuid);

    // Metody do historii przejazdów
    Page<Ride> findByDriverUuid(String driverUuid, Pageable pageable);
    Page<Ride> findByDriverUuidAndCreatedAtBetween(String driverUuid, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    Page<Ride> findByDriverUuidAndStatus(String driverUuid, RideStatus status, Pageable pageable);

    // Metody do statystyk
    List<Ride> findByDriverUuidAndCreatedAtBetweenAndIsPaidTrue(String driverUuid, LocalDateTime startDate, LocalDateTime endDate);
}