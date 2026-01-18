package com.uber.common.productSelector;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RideDataInfoDto implements Serializable {
    private double uberXPrice;
    private double uberComfortPrice;
    private double uberPetsPrice;
    private double uberGreenPrice;
    private int eta;
    private long distance;
    private String polyline;
}
