package com.client_app.model.ride_request;

import com.uber.common.Coordinates;
import com.uber.common.productSelector.Product;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class CreateRideRequestCommand {
    private String clientUuid;
    private Coordinates pickupLocation;
    private Coordinates destinationLocation;
    private Product product;
}