package com.uber.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DriverResponseDto {
    private String rideUuid;
    private String driverUuid;
    private boolean accepted;
    private String rejectionReason;
    private Integer estimatedArrivalTime;

}