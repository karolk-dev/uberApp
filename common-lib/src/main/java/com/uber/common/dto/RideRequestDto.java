package com.uber.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.uber.common.Coordinates;
import com.uber.common.productSelector.Product;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Builder;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RideRequestDto implements Serializable {
    private String clientUuid;
    private Coordinates pickupLocation;
    private Coordinates destinationLocation;
    private String status;
    private Product product;
    private String customerId;
    private String paymentMethodId;
}
