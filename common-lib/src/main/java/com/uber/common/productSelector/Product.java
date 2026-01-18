package com.uber.common.productSelector;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Product {
    UberX(1.0),
    UberComfort(1.5),
    UberPets(1.2),
    UberGreen(1.3);

    private final double priceMultiplier;
}
