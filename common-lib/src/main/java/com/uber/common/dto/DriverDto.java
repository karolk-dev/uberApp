package com.uber.common.dto;

import com.uber.common.Coordinates;
import com.uber.common.model.DriverStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class DriverDto {
    private String uuid;
    private String name;
    private String nip;
    private String companyName;
    private String companyStatus;
    private Coordinates coordinates;
    private boolean isAvailable;
    private DriverStatus status;
}