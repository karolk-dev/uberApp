package com.uber.common.productSelector;

import com.uber.common.Coordinates;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class RideDataDto implements Serializable {
    private Coordinates pickupLocation;
    private Coordinates destinationLocation;
    private Product product;
}
