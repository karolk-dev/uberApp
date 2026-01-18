package com.driver_app.repository;

import com.driver_app.model.Driver;
import com.uber.common.Coordinates;
import com.uber.common.model.CarCategory;
import com.uber.common.model.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByUuid(String uuid);

    @Query(value = "SELECT * FROM (" +
            "SELECT d.*, (6371 * acos(cos(radians(:#{#coordinates.latitude})) * cos(radians(d.location_latitude)) * cos(radians(d.location_longitude) - radians(:#{#coordinates.longitude})) + sin(radians(:#{#coordinates.latitude})) * sin(radians(d.location_latitude)))) AS distance " +
            "FROM driver d " +
            "WHERE d.is_available = true" +
            ") AS drivers_with_distance " +
            "WHERE distance <= :radius", nativeQuery = true)
    Set<Driver> findUsersWithinRadius(
            @Param("coordinates") Coordinates coordinates,
            @Param("radius") double radiusInKm
    );

    Set<Driver> findByIsAvailable(boolean isAvailable);

    @Query("SELECT d FROM Driver d")
    Set<Driver> findAllDrivers();

    Optional<Driver> findByName(String name);

    boolean existsByName(String name);
}