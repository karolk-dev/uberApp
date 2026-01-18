package com.client_app.mappings;

import com.client_app.model.ride_request.RideRequest;
import com.uber.common.dto.RideRequestDto;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Component;

@Component
public class RideRequestToRideRequestDtoConverter implements Converter<RideRequest, RideRequestDto> {

    @Override
    public RideRequestDto convert(MappingContext<RideRequest, RideRequestDto> context) {
        RideRequest source = context.getSource();
        return new RideRequestDto(
                source.getClientUuid(),
                source.getPickupLocation(),
                source.getDestinationLocation(),
                source.getStatus(),
                source.getProduct(),
                source.getCustomerId(),
                source.getPaymentMethodId()
        );
    }
}