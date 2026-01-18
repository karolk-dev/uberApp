package com.server_app.service;

import com.uber.common.productSelector.Product;
import org.springframework.stereotype.Service;

@Service
public class RideCalculator {
    private static final double BaseRatePerKM = 2.0;

    public static double calculateRidePrice(Product product, long distance) {
        long distanceInKm = distance / 1000;
        double multiplier = product.getPriceMultiplier();

        return (BaseRatePerKM * distanceInKm * multiplier);
    }
}
