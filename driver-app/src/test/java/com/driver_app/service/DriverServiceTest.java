package com.driver_app.service;

import com.driver_app.clients.CeidgApiClient;
import com.driver_app.clients.CeidgCompanyInfo;
import com.driver_app.clients.CeidgResponse;
import com.driver_app.exceptions.CeidgVerificationException;
import com.driver_app.exceptions.DuplicateDriverException;
import com.driver_app.exceptions.PenaltyException;
import com.driver_app.exceptions.RideFinishException;
import com.driver_app.exceptions.UserCreationException;
import com.driver_app.model.Driver;
import com.driver_app.repository.DriverRepository;
import com.uber.common.Coordinates;
import com.uber.common.command.CreateDriverCommand;
import com.uber.common.command.UpdateDriverLocationCommand;
import com.uber.common.dto.DriverDto;
import com.uber.common.model.DriverStatus;
import com.uber.common.model.DriverStatusUpdateRequest;
import com.uber.common.model.RideStatus;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private CeidgApiClient ceidgApiClient;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private Keycloak keycloak;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RealmResource realmResource;

    @Mock
    private UsersResource usersResource;

    @Mock
    private UserResource userResource;

    @InjectMocks
    private DriverService driverService;

    @BeforeEach
    public void setup() {

        ReflectionTestUtils.setField(driverService, "realm", "testRealm");
        lenient().when(keycloak.realm("testRealm")).thenReturn(realmResource);
        lenient().when(realmResource.users()).thenReturn(usersResource);
    }

    @Test
    public void testRegisterDriverDuplicateName() {

        CreateDriverCommand command = CreateDriverCommand.builder()
                .name("existingDriver")
                .nip("1234567890")
                .password("password")
                .build();
        CeidgCompanyInfo companyInfo = new CeidgCompanyInfo();
        companyInfo.setNazwa("Test Company");
        companyInfo.setStatus("ACTIVE");
        CeidgResponse ceidgResponse = new CeidgResponse();
        ceidgResponse.setFirma(Collections.singletonList(companyInfo));
        when(ceidgApiClient.getCompanyInfo("1234567890")).thenReturn(ceidgResponse);
        when(driverRepository.existsByName("existingDriver")).thenReturn(true);
        // when & then
        assertThrows(DuplicateDriverException.class, () -> driverService.registerDriver(command));
        verify(driverRepository).existsByName("existingDriver");
    }

    @Test
    public void testRegisterDriverCeidgVerificationFailure() {

        CreateDriverCommand command = CreateDriverCommand.builder()
                .name("newDriver")
                .nip("1234567890")
                .password("password")
                .build();
        CeidgResponse ceidgResponse = new CeidgResponse();
        ceidgResponse.setFirma(Collections.emptyList());
        when(ceidgApiClient.getCompanyInfo("1234567890")).thenReturn(ceidgResponse);
        // when & then
        assertThrows(CeidgVerificationException.class, () -> driverService.registerDriver(command));
        verify(ceidgApiClient).getCompanyInfo("1234567890");
    }


    @Test
    public void testRegisterDriverKeycloakCreationFailure() {

        CreateDriverCommand command = CreateDriverCommand.builder()
                .name("newDriver")
                .nip("1234567890")
                .password("password")
                .build();
        when(driverRepository.existsByName("newDriver")).thenReturn(false);
        CeidgCompanyInfo companyInfo = new CeidgCompanyInfo();
        companyInfo.setNazwa("Test Company");
        CeidgResponse ceidgResponse = new CeidgResponse();
        ceidgResponse.setFirma(Collections.singletonList(companyInfo));
        when(ceidgApiClient.getCompanyInfo("1234567890")).thenReturn(ceidgResponse);
        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(400);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        // when & then
        UserCreationException ex = assertThrows(UserCreationException.class, () -> driverService.registerDriver(command));
        assertTrue(ex.getMessage().contains("Nie udało się utworzyć użytkownika"));
        verify(usersResource).create(any(UserRepresentation.class));
    }

    @Test
    public void testRegisterDriverSuccess() {

        CreateDriverCommand command = CreateDriverCommand.builder()
                .name("newDriver")
                .nip("1234567890")
                .password("password")
                .build();
        when(driverRepository.existsByName("newDriver")).thenReturn(false);
        CeidgCompanyInfo companyInfo = new CeidgCompanyInfo();
        companyInfo.setNazwa("Test Company");
        companyInfo.setStatus("ACTIVE");
        CeidgResponse ceidgResponse = new CeidgResponse();
        ceidgResponse.setFirma(Collections.singletonList(companyInfo));
        when(ceidgApiClient.getCompanyInfo("1234567890")).thenReturn(ceidgResponse);
        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(201);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        try (MockedStatic<CreatedResponseUtil> mockedStatic = mockStatic(CreatedResponseUtil.class)) {

            mockedStatic.when(() -> CreatedResponseUtil.getCreatedId(response)).thenReturn("user123");
            when(usersResource.get("user123")).thenReturn(userResource);
            doNothing().when(userResource).resetPassword(any(CredentialRepresentation.class));
            Driver driver = Driver.builder()
                    .uuid("user123")
                    .name("newDriver")
                    .nip("1234567890")
                    .companyName("Test Company")
                    .coordinates(new Coordinates(50.24280000, 19.02330000))
                    .companyStatus("ACTIVE")
                    .status(DriverStatus.OFFLINE)
                    .isAvailable(true)
                    .build();
            when(driverRepository.save(any(Driver.class))).thenReturn(driver);
            DriverDto driverDto = new DriverDto();
            driverDto.setUuid("user123");
            driverDto.setName("newDriver");
            driverDto.setNip("1234567890");
            driverDto.setCompanyName("Test Company");
            when(modelMapper.map(driver, DriverDto.class)).thenReturn(driverDto);
            // when
            DriverDto result = driverService.registerDriver(command);
            // then
            assertNotNull(result);
            assertEquals("newDriver", result.getName());
            assertEquals("user123", result.getUuid());
        }
        verify(usersResource).create(any(UserRepresentation.class));
        verify(userResource).resetPassword(any(CredentialRepresentation.class));
        verify(driverRepository).save(any(Driver.class));
    }

    @Test
    public void testFinishRideSuccess() {

        String rideUuid = "ride123";
        RideStatus rideStatus = RideStatus.COMPLETED;
        ResponseEntity<RideStatus> responseEntity = new ResponseEntity<>(rideStatus, HttpStatus.OK);
        when(restTemplate.postForEntity("http://localhost:8081/chat/server-app/api/rides/finish/" + rideUuid,
                null, RideStatus.class)).thenReturn(responseEntity);
        // when
        RideStatus result = driverService.finishRide(rideUuid);
        // then
        assertEquals(rideStatus, result);
        verify(restTemplate).postForEntity("http://localhost:8081/chat/server-app/api/rides/finish/" + rideUuid,
                null, RideStatus.class);
    }

    @Test
    public void testFinishRideFailure() {
        // given:
        String rideUuid = "ride123";
        ResponseEntity<RideStatus> responseEntity = new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        when(restTemplate.postForEntity("http://localhost:8081/chat/server-app/api/rides/finish/" + rideUuid,
                null, RideStatus.class)).thenReturn(responseEntity);
        // when & then
        RideFinishException ex = assertThrows(RideFinishException.class, () -> driverService.finishRide(rideUuid));
        assertTrue(ex.getMessage().contains("Błąd podczas kończenia przejazdu"));
        verify(restTemplate).postForEntity("http://localhost:8081/chat/server-app/api/rides/finish/" + rideUuid,
                null, RideStatus.class);
    }

    @Test
    public void testUpdateDriverStatusSuccess() {
        // given:
        String driverUuid = "driver123";
        Driver driver = new Driver();
        driver.setUuid(driverUuid);
        driver.setStatus(DriverStatus.OFFLINE);
        driver.setAvailable(true);
        when(driverRepository.findByUuid(driverUuid)).thenReturn(Optional.of(driver));
        DriverStatusUpdateRequest updateRequest = new DriverStatusUpdateRequest();
        updateRequest.setStatus(DriverStatus.AVAILABLE);
        updateRequest.setAvailable(false);
        // when
        driverService.updateDriverStatus(driverUuid, updateRequest);
        // then
        assertEquals(DriverStatus.AVAILABLE, driver.getStatus());
        assertFalse(driver.isAvailable());
        verify(driverRepository).findByUuid(driverUuid);
        verify(driverRepository).save(driver);
    }

    @Test
    public void testUpdateDriverStatusDriverNotFound() {
        // given:
        String driverUuid = "driver123";
        when(driverRepository.findByUuid(driverUuid)).thenReturn(Optional.empty());
        DriverStatusUpdateRequest updateRequest = new DriverStatusUpdateRequest();
        updateRequest.setStatus(DriverStatus.AVAILABLE);
        // when & then
        assertThrows(RuntimeException.class, () -> driverService.updateDriverStatus(driverUuid, updateRequest));
        verify(driverRepository).findByUuid(driverUuid);
    }

    @Test
    public void testUpdateDriverLocationSuccess() {
        // given:
        String driverUuid = "driver123";
        Driver driver = new Driver();
        driver.setUuid(driverUuid);
        Coordinates oldCoordinates = new Coordinates(50.0, 19.0);
        driver.setCoordinates(oldCoordinates);
        when(driverRepository.findByUuid(driverUuid)).thenReturn(Optional.of(driver));
        UpdateDriverLocationCommand command = new UpdateDriverLocationCommand();
        Coordinates newCoordinates = new Coordinates(51.0, 20.0);
        command.setNewCoordinates(newCoordinates);
        // when
        driverService.updateDriverLocation(driverUuid, command);
        // then
        assertEquals(newCoordinates, driver.getCoordinates());
        verify(driverRepository).findByUuid(driverUuid);
        verify(driverRepository).save(driver);
    }

    @Test
    public void testUpdateDriverLocationDriverNotFound() {
        // given:
        String driverUuid = "driver123";
        when(driverRepository.findByUuid(driverUuid)).thenReturn(Optional.empty());
        UpdateDriverLocationCommand command = new UpdateDriverLocationCommand();
        command.setNewCoordinates(new Coordinates(51.0, 20.0));
        // when & then
        assertThrows(RuntimeException.class, () -> driverService.updateDriverLocation(driverUuid, command));
        verify(driverRepository).findByUuid(driverUuid);
    }

    @Test
    public void testGetDriversWithAvailableParameter() {
        // given:
        Driver driver = new Driver();
        driver.setUuid("driver1");
        when(driverRepository.findByIsAvailable(true)).thenReturn(new HashSet<>(Collections.singletonList(driver)));
        DriverDto driverDto = new DriverDto();
        driverDto.setUuid("driver1");
        when(modelMapper.map(driver, DriverDto.class)).thenReturn(driverDto);
        // when
        Set<DriverDto> result = driverService.getDrivers(true);
        // then
        assertEquals(1, result.size());
        assertTrue(result.stream().anyMatch(dto -> dto.getUuid().equals("driver1")));
        verify(driverRepository).findByIsAvailable(true);
    }

    @Test
    public void testGetDriversWithoutParameter() {
        // given:
        Driver driver = new Driver();
        driver.setUuid("driver1");
        when(driverRepository.findAllDrivers()).thenReturn(new HashSet<>(Collections.singletonList(driver)));
        DriverDto driverDto = new DriverDto();
        driverDto.setUuid("driver1");
        when(modelMapper.map(driver, DriverDto.class)).thenReturn(driverDto);
        // when
        Set<DriverDto> result = driverService.getDrivers(null);
        // then
        assertEquals(1, result.size());
        assertTrue(result.stream().anyMatch(dto -> dto.getUuid().equals("driver1")));
        verify(driverRepository).findAllDrivers();
    }

    @Test
    public void testGetDriversWithinRadius() {
        // given:
        Coordinates coordinates = new Coordinates(50.0, 19.0);
        double radius = 10.0;
        Driver driver = new Driver();
        driver.setUuid("driver1");
        when(driverRepository.findUsersWithinRadius(coordinates, radius))
                .thenReturn(new HashSet<>(Collections.singletonList(driver)));
        DriverDto driverDto = new DriverDto();
        driverDto.setUuid("driver1");
        when(modelMapper.map(driver, DriverDto.class)).thenReturn(driverDto);
        // when
        Set<DriverDto> result = driverService.getDriversWithinRadius(coordinates, radius);
        // then
        assertEquals(1, result.size());
        assertTrue(result.stream().anyMatch(dto -> dto.getUuid().equals("driver1")));
        verify(driverRepository).findUsersWithinRadius(coordinates, radius);
    }

    @Test
    public void testPenaltySuccess() {
        // given:
        String rideUuid = "ride123";
        ResponseEntity<?> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.postForEntity("http://localhost:8081/chat/server-app/api/rides/penalty/" + rideUuid,
                null, Void.class)).thenReturn((ResponseEntity<Void>) responseEntity);
        // when
        Object result = driverService.penalty(rideUuid);
        // then
        assertNull(result);
        verify(restTemplate).postForEntity("http://localhost:8081/chat/server-app/api/rides/penalty/" + rideUuid,
                null, Void.class);
    }

    @Test
    public void testPenaltyFailure() {
        // given:
        String rideUuid = "ride123";
        ResponseEntity<?> responseEntity = new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        when(restTemplate.postForEntity("http://localhost:8081/chat/server-app/api/rides/penalty/" + rideUuid,
                null, Void.class)).thenReturn((ResponseEntity<Void>) responseEntity);
        // when & then
        PenaltyException ex = assertThrows(PenaltyException.class, () -> driverService.penalty(rideUuid));
        assertTrue(ex.getMessage().contains("Błąd podczas wymierzania kary"));
        verify(restTemplate).postForEntity("http://localhost:8081/chat/server-app/api/rides/penalty/" + rideUuid,
                null, Void.class);
    }
}
