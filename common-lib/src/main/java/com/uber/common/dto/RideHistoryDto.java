package com.uber.common.dto;

import com.uber.common.model.PaymentType;
import com.uber.common.model.RideStatus;
import com.uber.common.productSelector.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RideHistoryDto {
    private String rideUuid;
    private LocalDateTime createdAt;
    private Double pickupLocationLongitude;
    private Double pickupLocationLatitude;
    private Double destinationLongitude;
    private Double destinationLatitude;
    private RideStatus status;
    private BigDecimal amount;
    private String currency;
    private boolean isPaid;
    private PaymentType paymentType;

    private String clientName;
    private String driverName;
    private Product product;
}