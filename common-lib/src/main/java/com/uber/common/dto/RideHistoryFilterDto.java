package com.uber.common.dto;

import com.uber.common.model.PaymentType;
import com.uber.common.model.RideStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RideHistoryFilterDto {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private RideStatus status;
    private Boolean isPaid;
    private PaymentType paymentType;
    private BigDecimal minFare;
    private BigDecimal maxFare;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;
}