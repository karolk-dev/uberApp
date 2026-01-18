package com.server_app.model.command;

import com.uber.common.model.RideStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class EditRideCommand {
    private String uuid;
    private String clientUuid;
    private String driverUuid;

    private Double pickupLocationLatitude;
    private Double pickupLocationLongitude;

    private Double destinationLatitude;
    private Double destinationLongitude;

    @Enumerated(EnumType.STRING)
    private RideStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long penaltyAmount;
}
