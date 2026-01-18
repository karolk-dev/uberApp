package com.server_app.dto;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.util.Map;

@Getter
@Builder
public class EarningsSummary {
    private BigDecimal totalEarnings;
    private int totalRides;
    private Map<String, BigDecimal> earningsByPaymentType;
    private BigDecimal averagePerRide;
}