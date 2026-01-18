package com.uber.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class StartRideRequest {
    private String polyline;
    private String clientUuid;
}
