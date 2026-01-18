package com.driver_app.controller;

import com.driver_app.service.CarService;
import com.uber.common.command.CreateCarCommand;
import com.uber.common.dto.CarDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Set;

@RestController
@RequestMapping("/api/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    @PostMapping("/driver/{driverUuid}")
    public ResponseEntity<CarDto> addCarToDriver(
            @PathVariable String driverUuid,
            @Valid @RequestBody CreateCarCommand command) {
        CarDto carDto = carService.addCarToDriver(driverUuid, command);
        return ResponseEntity.status(HttpStatus.CREATED).body(carDto);
    }

    @PutMapping("/{carUuid}/status")
    public ResponseEntity<Void> setCarActiveStatus(
            @PathVariable String carUuid,
            @RequestParam boolean isActive) {
        carService.setCarActiveStatus(carUuid, isActive);
        return ResponseEntity.noContent().build();
    }

//    @GetMapping("/driver/{driverUuid}")
//    public ResponseEntity<Set<CarDto>> getDriverCars(@PathVariable String driverUuid) {
//        Set<CarDto> cars = carService.getDriverCars(driverUuid);
//        return ResponseEntity.ok(cars);
//    }

    @GetMapping("/{carUuid}")
    public ResponseEntity<CarDto> getCarByUuid(@PathVariable String carUuid) {
        CarDto carDto = carService.getCarByUuid(carUuid);
        return ResponseEntity.ok(carDto);
    }
}