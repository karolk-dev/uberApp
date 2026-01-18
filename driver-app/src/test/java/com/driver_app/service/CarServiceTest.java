package com.driver_app.service;

import com.driver_app.exceptions.CarNotFoundException;
import com.driver_app.exceptions.DriverNotFoundException;
import com.driver_app.mappings.CarToCarDtoConverter;
import com.driver_app.model.Car;
import com.driver_app.model.Driver;
import com.driver_app.repository.CarRepository;
import com.driver_app.repository.DriverRepository;
import com.uber.common.command.CreateCarCommand;
import com.uber.common.dto.CarDto;
import com.uber.common.model.CarCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.spi.MappingContext;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CarServiceTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private DriverService driverService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CarService carService;

    private Driver driver;
    private CreateCarCommand createCarCommand;
    private Car car;

    @BeforeEach
    public void setup() {
        driver = new Driver();
        driver.setUuid("driver-123");

        createCarCommand = new CreateCarCommand();
        createCarCommand.setModel("Model S");
        createCarCommand.setMake("Tesla");
        createCarCommand.setLicensePlate("ABC123");
        createCarCommand.setCategory(CarCategory.STANDARD);

        car = Car.builder()
                .uuid(UUID.randomUUID().toString())
                .model("Model S")
                .make("Tesla")
                .licensePlate("ABC123")
                .category(CarCategory.STANDARD)
                .isActive(true)
                .build();
    }

    @Test
    public void testAddCarToDriverSuccess() {
        // given
        when(driverRepository.findByUuid("driver-123")).thenReturn(Optional.of(driver));
        when(carRepository.save(any(Car.class))).thenReturn(car);
        CarDto carDto = new CarDto();
        carDto.setUuid(car.getUuid());
        carDto.setModel(car.getModel());
        carDto.setMake(car.getMake());
        carDto.setLicensePlate(car.getLicensePlate());
        carDto.setCategory(car.getCategory());
        when(modelMapper.map(car, CarDto.class)).thenReturn(carDto);
        // when
        CarDto result = carService.addCarToDriver("driver-123", createCarCommand);
        // then
        assertNotNull(result);
        assertEquals(car.getUuid(), result.getUuid());
        verify(driverRepository).findByUuid("driver-123");
        verify(carRepository).save(any(Car.class));
    }

    @Test
    public void testAddCarToDriverDriverNotFound() {
        // given
        when(driverRepository.findByUuid("driver-123")).thenReturn(Optional.empty());
        // when & then
        assertThrows(DriverNotFoundException.class,
                () -> carService.addCarToDriver("driver-123", createCarCommand));
        verify(driverRepository).findByUuid("driver-123");
    }

    @Test
    public void testSetCarActiveStatusSuccess() {
        // given
        when(carRepository.findByUuid("car-123")).thenReturn(Optional.of(car));
        // when
        carService.setCarActiveStatus("car-123", false);
        // then
        assertFalse(car.isActive());
        verify(carRepository).findByUuid("car-123");
        verify(carRepository).save(car);
    }

    @Test
    public void testSetCarActiveStatusCarNotFound() {
        // given
        when(carRepository.findByUuid("car-123")).thenReturn(Optional.empty());
        // when & then
        assertThrows(CarNotFoundException.class,
                () -> carService.setCarActiveStatus("car-123", false));
        verify(carRepository).findByUuid("car-123");
    }

    @Test
    public void testGetCarByUuidSuccess() {
        // given
        when(carRepository.findByUuid("car-123")).thenReturn(Optional.of(car));
        CarDto carDto = new CarDto();
        carDto.setUuid(car.getUuid());
        carDto.setModel(car.getModel());
        carDto.setMake(car.getMake());
        carDto.setLicensePlate(car.getLicensePlate());
        carDto.setCategory(car.getCategory());
        when(modelMapper.map(car, CarDto.class)).thenReturn(carDto);
        // when
        CarDto result = carService.getCarByUuid("car-123");
        // then
        assertNotNull(result);
        assertEquals(car.getUuid(), result.getUuid());
        verify(carRepository).findByUuid("car-123");
    }

    @Test
    public void testGetCarByUuidCarNotFound() {
        // given
        when(carRepository.findByUuid("car-123")).thenReturn(Optional.empty());
        // when & then
        assertThrows(CarNotFoundException.class, () -> carService.getCarByUuid("car-123"));
        verify(carRepository).findByUuid("car-123");
    }

    @Test
    public void testCarMapping() {
        // given
        Car car = Car.builder()
                .uuid("simple-uuid")
                .make("SimpleMake")
                .model("SimpleModel")
                .licensePlate("SIM123")
                .category(CarCategory.STANDARD)
                .isActive(true)
                .build();
        CarToCarDtoConverter converter = new CarToCarDtoConverter();
        MappingContext<Car, CarDto> context = mock(MappingContext.class);
        when(context.getSource()).thenReturn(car);
        // when
        CarDto dto = converter.convert(context);
        // then
        assertNotNull(dto);
        assertEquals("simple-uuid", dto.getUuid());
        assertEquals("SimpleMake", dto.getMake());
        assertEquals("SimpleModel", dto.getModel());
        assertEquals("SIM123", dto.getLicensePlate());
        assertEquals(CarCategory.STANDARD, dto.getCategory());
        assertTrue(dto.isActive());
    }

}
