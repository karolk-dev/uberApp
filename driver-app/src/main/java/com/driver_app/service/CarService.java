package com.driver_app.service;

import com.driver_app.exceptions.CarNotFoundException;
import com.driver_app.exceptions.DriverNotFoundException;
import com.driver_app.model.Car;
import com.driver_app.model.Driver;
import com.driver_app.repository.CarRepository;
import com.driver_app.repository.DriverRepository;
import com.uber.common.command.CreateCarCommand;
import com.uber.common.dto.CarDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository carRepository;
    private final DriverRepository driverRepository;
    private final DriverService driverService;
    private final ModelMapper modelMapper;

    @Transactional
    public CarDto addCarToDriver(String driverUuid, CreateCarCommand command) {
        Driver driver = findDriverByUuid(driverUuid);

        Car car = Car.builder()
                .uuid(UUID.randomUUID().toString())
                .model(command.getModel())
                .make(command.getMake())
                .licensePlate(command.getLicensePlate())
                .category(command.getCategory())
                .isActive(true)
                .build();

        Car savedCar = carRepository.save(car);

        return modelMapper.map(savedCar, CarDto.class);
    }

    @Transactional
    public void setCarActiveStatus(String carUuid, boolean isActive) {
        Car car = findCarByUuid(carUuid);

        car.setActive(isActive);
        carRepository.save(car);
    }

    @Transactional(readOnly = true)
    public CarDto getCarByUuid(String carUuid) {
        Car car = findCarByUuid(carUuid);
        return modelMapper.map(car, CarDto.class);
    }

    private Driver findDriverByUuid(String uuid) {
        return driverRepository.findByUuid(uuid)
                .orElseThrow(() -> new DriverNotFoundException("Driver not found: " + uuid));
    }

    private Car findCarByUuid(String uuid) {
        return carRepository.findByUuid(uuid)
                .orElseThrow(() -> new CarNotFoundException("Car not found: " + uuid));
    }
}