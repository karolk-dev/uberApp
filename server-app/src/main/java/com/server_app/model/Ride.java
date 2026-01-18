package com.server_app.model;

import com.uber.common.Coordinates;
import com.uber.common.productSelector.Product;
import com.querydsl.core.annotations.QueryEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import com.uber.common.model.PaymentType;
import com.uber.common.model.RideStatus;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@QueryEntity
public class Ride {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String uuid;
    private String clientUuid;
    private String driverUuid;

    private Double pickupLocationLatitude;
    private Double pickupLocationLongitude;

    private Double destinationLatitude;
    private Double destinationLongitude;

    @Enumerated(EnumType.STRING)
    private RideStatus status;

    @Enumerated(EnumType.STRING)
    private Product product;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private OffsetDateTime searchStartTime;

    private long amount;
    private long penaltyAmount;

    private String paymentIntendId;

    private String customerId;

    @Column(name = "currency")
    private String currency;
    private boolean isPaid;
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    private String polyline;
    private String polylineToClient;

    @Column(name = "client_name")
    private String clientName;

    @Column(name = "driver_name")
    private String driverName;
}
