package com.uber.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.uber.common.Coordinates;
import com.uber.common.model.CarCategory;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RideProposalDto implements Serializable {
    private String rideUuid;
    private String driverUuid;
    private String clientUuid;
    private Double pickupLocationLongitude;
    private Double pickupLocationLatitude;
    private Double destinationLongitude;
    private Double destinationLatitude;
    private String polyline;
    private String polylineToClient;
}