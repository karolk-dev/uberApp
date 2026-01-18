package com.uber.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RideResponseDto implements Serializable {
    private String driverUuid;
    private String rideUuid;
    private boolean accepted;
    private String clientUuid;
}
