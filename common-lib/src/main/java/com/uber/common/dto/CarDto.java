package com.uber.common.dto;

import com.uber.common.model.CarCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CarDto {
    private String uuid;
    private String make;
    private String model;
    private String licensePlate;
    private CarCategory category;
    private boolean isActive;
}