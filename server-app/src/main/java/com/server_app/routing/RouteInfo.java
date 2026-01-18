package com.server_app.routing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteInfo {
    private int etaInMinutes;
    private long distanceInMeters;
    private String polyline;
}
