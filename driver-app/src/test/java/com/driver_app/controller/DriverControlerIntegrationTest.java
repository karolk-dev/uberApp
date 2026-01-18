package com.driver_app.controller;

import com.driver_app.model.Driver;
import com.driver_app.repository.DriverRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uber.common.Coordinates;
import com.uber.common.dto.DriverDto;
import com.uber.common.model.DriverStatus;
import com.uber.common.model.DriverStatusUpdateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc
@EmbeddedKafka
public class DriverControlerIntegrationTest {

    private final MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DriverRepository driverRepository;

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:15")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    @Autowired
    public DriverControlerIntegrationTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }


    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.flyway.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.flyway.user", postgreSQLContainer::getUsername);
        registry.add("spring.flyway.password", postgreSQLContainer::getPassword);
        registry.add("eureka.client.register-with-eureka", () -> "false");
        registry.add("eureka.client.fetch-registry", () -> "false");

    }

    @Test
    void getDriversWithinRadius_shouldReturnCorrectDrivers() throws Exception {
        //Given
        Coordinates coords = new Coordinates();
        coords.setLatitude(21.0122);
        coords.setLongitude(52.2297);

        Driver driver1 = Driver.builder()
                .coordinates(coords)
                .uuid("abc-123")
                .name("Jan Nowak")
                .nip("1234567890")
                .companyName("Firma Transportowa")
                .companyStatus("ACTIVE")
                .isAvailable(true)
                .status(DriverStatus.AVAILABLE)
                .build();
        driverRepository.save(driver1);


        // when & then
        mockMvc.perform(get("/driver-app/api/drivers/range")
                        .param("latitude", "21.0122")
                        .param("longitude", "52.2297")
                        .param("radiusInKm", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].uuid").value("abc-123"))
                .andExpect(jsonPath("$[0].name").value("Jan Nowak"))
                .andExpect(jsonPath("$[0].nip").value("1234567890"))
                .andExpect(jsonPath("$[0].companyName").value("Firma Transportowa"))
                .andExpect(jsonPath("$[0].companyStatus").value("ACTIVE"))
                .andExpect(jsonPath("$[0].coordinates.latitude").value(21.0122))
                .andExpect(jsonPath("$[0].coordinates.longitude").value(52.2297))
                .andExpect(jsonPath("$[0].available").value(true));

    }

    @Test
    void shouldUpdateDriverStatus() throws Exception {
        // Given
        Coordinates coords = new Coordinates();
        coords.setLatitude(21.0122);
        coords.setLongitude(52.2297);
        Driver driver1 = Driver.builder()
                .coordinates(coords)
                .uuid("abc-123")
                .name("Jan Nowak")
                .nip("1234567890")
                .companyName("Firma Transportowa")
                .companyStatus("ACTIVE")
                .isAvailable(true)
                .status(DriverStatus.AVAILABLE)
                .build();
        driverRepository.save(driver1);
        String driverUuid = "abc-123";
        DriverStatusUpdateRequest request = new DriverStatusUpdateRequest();
        request.setStatus(DriverStatus.AVAILABLE);

        // When & Then
        mockMvc.perform(put("/driver-app/api/drivers/abc-123/status", driverUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

    }



}
