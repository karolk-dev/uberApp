package com.server_app.dto;

import com.uber.common.model.PaymentType;
import com.uber.common.model.RideStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RideHistorySearchCriteria {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private RideStatus status;
    private Boolean isPaid;
    private PaymentType paymentType;
}