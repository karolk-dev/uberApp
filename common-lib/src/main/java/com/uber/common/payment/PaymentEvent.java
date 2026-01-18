package com.uber.common.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class PaymentEvent {
    private Long paymentId;
    private Long rideId;
    private Long amount;
    private String currency;
    private String status;
    private LocalDateTime timestamp;
}
