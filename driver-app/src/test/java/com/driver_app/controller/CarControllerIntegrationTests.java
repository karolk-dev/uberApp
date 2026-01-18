package com.driver_app.controller;

import com.driver_app.service.CarService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uber.common.command.CreateCarCommand;
import com.uber.common.dto.CarDto;
import com.uber.common.model.CarCategory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CarController.class)
public class CarControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CarService carService;

    @Test
    public void addCarToDriver_shouldReturnCreatedCarDto() throws Exception {
        // Given
        String driverUuid = UUID.randomUUID().toString();
        CreateCarCommand command = createSampleCarCommand();
        CarDto expectedCarDto = createSampleCarDto();

        when(carService.addCarToDriver(eq(driverUuid), any(CreateCarCommand.class))).thenReturn(expectedCarDto);

        // When & Then
        mockMvc.perform(post("/api/cars/driver/{driverUuid}", driverUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid", is(expectedCarDto.getUuid())))
                .andExpect(jsonPath("$.make", is(expectedCarDto.getMake())))
                .andExpect(jsonPath("$.model", is(expectedCarDto.getModel())))
                .andExpect(jsonPath("$.licensePlate", is(expectedCarDto.getLicensePlate())))
                .andExpect(jsonPath("$.category", is(expectedCarDto.getCategory().toString())))
                .andExpect(jsonPath("$.active", is(expectedCarDto.isActive())));

        verify(carService, times(1)).addCarToDriver(eq(driverUuid), any(CreateCarCommand.class));
    }

    @Test
    public void setCarActiveStatus_shouldReturnNoContent() throws Exception {
        // Given
        String carUuid = UUID.randomUUID().toString();
        boolean isActive = true;

        doNothing().when(carService).setCarActiveStatus(eq(carUuid), eq(isActive));

        // When & Then
        mockMvc.perform(put("/api/cars/{carUuid}/status", carUuid)
                        .param("isActive", String.valueOf(isActive)))
                .andExpect(status().isNoContent());

        verify(carService, times(1)).setCarActiveStatus(eq(carUuid), eq(isActive));
    }

    @Test
    public void getCarByUuid_shouldReturnCarDto() throws Exception {
        // Given
        String carUuid = UUID.randomUUID().toString();
        CarDto expectedCarDto = createSampleCarDto();

        when(carService.getCarByUuid(eq(carUuid))).thenReturn(expectedCarDto);

        // When & Then
        mockMvc.perform(get("/api/cars/{carUuid}", carUuid))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid", is(expectedCarDto.getUuid())))
                .andExpect(jsonPath("$.make", is(expectedCarDto.getMake())))
                .andExpect(jsonPath("$.model", is(expectedCarDto.getModel())))
                .andExpect(jsonPath("$.licensePlate", is(expectedCarDto.getLicensePlate())))
                .andExpect(jsonPath("$.category", is(expectedCarDto.getCategory().toString())))
                .andExpect(jsonPath("$.active", is(expectedCarDto.isActive())));

        verify(carService, times(1)).getCarByUuid(eq(carUuid));
    }

    @Test
    public void addCarToDriver_withInvalidData_shouldReturnBadRequest() throws Exception {
        // Given
        String driverUuid = UUID.randomUUID().toString();
        CreateCarCommand invalidCommand = new CreateCarCommand(); // Empty command with no data

        // When & Then
        mockMvc.perform(post("/api/cars/driver/{driverUuid}", driverUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCommand)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("Bad Request")))
                .andExpect(jsonPath("$.code", is(400)))
                .andExpect(jsonPath("$.message").exists());

        // Verify that service was never called with invalid data
        verify(carService, times(0)).addCarToDriver(anyString(), any(CreateCarCommand.class));
    }

    // Helper methods to create test data
    private CreateCarCommand createSampleCarCommand() {
        CreateCarCommand command = new CreateCarCommand();
        command.setMake("Toyota");
        command.setModel("Camry");
        command.setLicensePlate("ABC-123");
        command.setCategory(CarCategory.STANDARD);
        return command;
    }

    private CarDto createSampleCarDto() {
        CarDto carDto = new CarDto();
        carDto.setUuid(UUID.randomUUID().toString());
        carDto.setMake("Toyota");
        carDto.setModel("Camry");
        carDto.setLicensePlate("ABC-123");
        carDto.setCategory(CarCategory.STANDARD);
        carDto.setActive(true);
        return carDto;
    }

    /*
    // Uncomment if you want to add test for the commented getDriverCars endpoint
    @Test
    public void getDriverCars_shouldReturnSetOfCarDto() throws Exception {
        // Given
        String driverUuid = UUID.randomUUID().toString();
        Set<CarDto> expectedCars = new HashSet<>();
        expectedCars.add(createSampleCarDto());
        expectedCars.add(createSampleCarDto());

        when(carService.getDriverCars(eq(driverUuid))).thenReturn(expectedCars);

        // When & Then
        mockMvc.perform(get("/api/cars/driver/{driverUuid}", driverUuid))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(expectedCars.size()));

        verify(carService, times(1)).getDriverCars(eq(driverUuid));
    }
    */
}