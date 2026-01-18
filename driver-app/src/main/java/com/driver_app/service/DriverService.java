package com.driver_app.service;

import com.driver_app.clients.CeidgApiClient;
import com.driver_app.clients.CeidgCompanyInfo;
import com.driver_app.clients.CeidgResponse;
import com.driver_app.exceptions.*;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.keycloak.admin.client.CreatedResponseUtil.getCreatedId;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverService {

    private final DriverRepository driverRepository;
    private final CeidgApiClient ceidgApiClient;
    private final ModelMapper modelMapper;
    private final Keycloak keycloak;
    private final RestTemplate restTemplate;
    @Value("${keycloak.realm}")
    private String realm;

    private String baseUrl = "http://localhost:8081/chat/server-app/api/rides";

    @Transactional
    public DriverDto registerDriver(CreateDriverCommand command) {
        CeidgCompanyInfo companyInfo = fetchCompanyInfoFromCeidg(command.getNip());
        if (driverRepository.existsByName(command.getName())) {
            throw new DuplicateDriverException("Username already taken");
        }

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(command.getName());
        userRepresentation.setEnabled(true);

        log.info("ok+++++");

        Response response = keycloak.realm(realm).users()
                .create(userRepresentation);

        log.info("ok1+++++");

        if (response.getStatus() != 201) {
            throw new UserCreationException("Nie udało się utworzyć użytkownika. Kod: " + response.getStatus());
        }

        String userId = getCreatedId(response);
        response.close();

        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(command.getPassword());

        keycloak.realm(realm).users().get(userId).resetPassword(passwordCred);

        Driver driver = buildDriverFromCommand(command, companyInfo, userId);
        return saveAndMapDriver(driver);
    }

    public RideStatus finishRide(String rideUuid) {
        String url = baseUrl + "/finish/" + rideUuid;

        ResponseEntity<RideStatus> response = restTemplate.postForEntity(url, null, RideStatus.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RideFinishException("Błąd podczas kończenia przejazdu. Status: " + response.getStatusCode());
        }

    }

    @Transactional
    public void updateDriverStatus(String driverUuid, DriverStatusUpdateRequest updateRequest) {
        Driver driver = findDriverByUuid(driverUuid);
        updateDriverStatusFields(driver, updateRequest);
        driverRepository.save(driver);
    }

    @Transactional
    public void updateDriverLocation(String driverUuid, UpdateDriverLocationCommand command) {
        Driver driver = findDriverByUuid(driverUuid);
        driver.setCoordinates(command.getNewCoordinates());
        driverRepository.save(driver);
    }

    @Transactional(readOnly = true)
    public Set<DriverDto> getDrivers(Boolean available) {
        Set<Driver> drivers = (available != null)
                ? driverRepository.findByIsAvailable(available)
                : driverRepository.findAllDrivers();

        return drivers.stream()
                .map(driver -> modelMapper.map(driver, DriverDto.class))
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public Set<DriverDto> getDriversWithinRadius(Coordinates coordinates, double radiusInKm) {
        return driverRepository.findUsersWithinRadius(coordinates, radiusInKm).stream()
                .map(driver -> modelMapper.map(driver, DriverDto.class))
                .collect(Collectors.toSet());

    }

    private CeidgCompanyInfo fetchCompanyInfoFromCeidg(String nip) {
        CeidgResponse response = ceidgApiClient.getCompanyInfo(nip);

        if (response == null || response.getFirma() == null || response.getFirma().isEmpty()) {
            throw new CeidgVerificationException("Company information not found in CEIDG");
        }

        return response.getFirma().getFirst();
    }

    private Driver buildDriverFromCommand(CreateDriverCommand command, CeidgCompanyInfo companyInfo, String uuid) {
        return Driver.builder()
                .uuid(uuid)
                .name(command.getName())
                .nip(command.getNip())
                .companyName(companyInfo.getNazwa())
                .coordinates(new Coordinates(50.24280000, 19.02330000)) //todo startowe koordynaty
                .companyStatus(Optional.ofNullable(companyInfo.getStatus()).orElse("UNKNOWN"))
                .status(DriverStatus.OFFLINE)
                .isAvailable(true)
                .build();
    }

    private Driver findDriverByUuid(String uuid) {
        return driverRepository.findByUuid(uuid)
                .orElseThrow(() -> new DriverNotFoundException("Driver not found with UUID: " + uuid));
    }

    private DriverDto saveAndMapDriver(Driver driver) {
        Driver savedDriver = driverRepository.save(driver);
        return modelMapper.map(savedDriver, DriverDto.class);
    }

    private void updateDriverStatusFields(Driver driver, DriverStatusUpdateRequest updateRequest) {
        if (updateRequest.getStatus() != null) {
            driver.setStatus(updateRequest.getStatus());
        }
        if (updateRequest.getAvailable() != null) {
            driver.setAvailable(updateRequest.getAvailable());
        }
    }

    public Object penalty(String rideUuid) {
        String url = baseUrl + "/penalty/" + rideUuid;
        ResponseEntity<?> response = restTemplate.postForEntity(url, null, Void.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new PenaltyException("Błąd podczas wymierzania kary. Status: " + response.getStatusCode());
        }

    }

}