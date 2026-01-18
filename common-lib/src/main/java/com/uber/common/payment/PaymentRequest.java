package com.uber.common.payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {
    private Integer amount;
    private String CustomerId;
    private Long estimatedAmount;
    private String paymentMethodId;
}
