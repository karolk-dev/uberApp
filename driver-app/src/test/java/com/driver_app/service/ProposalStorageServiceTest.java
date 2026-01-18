package com.driver_app.service;

import com.driver_app.clients.ServerAppClient;
import com.driver_app.exceptions.ProposalRemoveException;
import com.driver_app.exceptions.ProposalRetrieveException;
import com.driver_app.exceptions.ProposalStoreException;
import com.uber.common.dto.RideProposalDto;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProposalStorageServiceTest {

    @Mock
    private ServerAppClient serverAppClient;

    @InjectMocks
    private ProposalStorageService proposalStorageService;

    private RideProposalDto rideProposalDto;
    private final String driverUuid = "driver-123";
    private final String proposalUuid = "proposal-123";

    @BeforeEach
    public void setup() {
        rideProposalDto = new RideProposalDto();
        rideProposalDto.setDriverUuid(driverUuid);
        rideProposalDto.setRideUuid(proposalUuid);
    }

    @Test
    public void testStoreProposalSuccess() {
        // given
        // when
        proposalStorageService.storeProposal(rideProposalDto);
        // then
        verify(serverAppClient).storeProposal(rideProposalDto);
    }

    @Test
    public void testStoreProposalThrowsProposalStoreException() {
        // given
        FeignException feignException = new FeignException.InternalServerError(
                "Store error",
                Request.create(
                        Request.HttpMethod.POST,
                        "/store",
                        Collections.emptyMap(),
                        null,
                        StandardCharsets.UTF_8,
                        null
                ),
                null,
                null
        );
        doThrow(feignException).when(serverAppClient).storeProposal(rideProposalDto);
        // when & then
        ProposalStoreException exception = assertThrows(ProposalStoreException.class,
                () -> proposalStorageService.storeProposal(rideProposalDto));
        assertTrue(exception.getMessage().contains("Failed to store proposal"));
        verify(serverAppClient).storeProposal(rideProposalDto);
    }

    @Test
    public void testGetProposalForDriverSuccess() {
        // given
        ResponseEntity<RideProposalDto> responseEntity = ResponseEntity.ok(rideProposalDto);
        when(serverAppClient.getProposal(driverUuid)).thenReturn(responseEntity);
        // when
        Optional<RideProposalDto> result = proposalStorageService.getProposalForDriver(driverUuid);
        // then
        assertTrue(result.isPresent());
        assertEquals(rideProposalDto, result.get());
        verify(serverAppClient).getProposal(driverUuid);
    }

    @Test
    public void testGetProposalForDriverNotFound() {
        // given
        FeignException.NotFound notFoundException = new FeignException.NotFound(
                "Not found",
                Request.create(
                        Request.HttpMethod.GET,
                        "/proposal",
                        Collections.emptyMap(),
                        null,
                        StandardCharsets.UTF_8,
                        null
                ),
                null,
                null
        );
        when(serverAppClient.getProposal(driverUuid)).thenThrow(notFoundException);
        // when
        Optional<RideProposalDto> result = proposalStorageService.getProposalForDriver(driverUuid);
        // then
        assertFalse(result.isPresent());
        verify(serverAppClient).getProposal(driverUuid);
    }

    @Test
    public void testGetProposalForDriverThrowsProposalRetrieveException() {
        // given
        FeignException feignException = new FeignException.InternalServerError(
                "Retrieve error",
                Request.create(
                        Request.HttpMethod.GET,
                        "/proposal",
                        Collections.emptyMap(),
                        null,
                        StandardCharsets.UTF_8,
                        null
                ),
                null,
                null
        );
        when(serverAppClient.getProposal(driverUuid)).thenThrow(feignException);
        // when & then
        ProposalRetrieveException exception = assertThrows(ProposalRetrieveException.class,
                () -> proposalStorageService.getProposalForDriver(driverUuid));
        assertTrue(exception.getMessage().contains("Failed to get proposal"));
        verify(serverAppClient).getProposal(driverUuid);
    }

    @Test
    public void testRemoveProposalSuccess() {
        // given
        // when
        proposalStorageService.removeProposal(proposalUuid);
        // then
        verify(serverAppClient).removeProposal(proposalUuid);
    }

    @Test
    public void testRemoveProposalThrowsProposalRemoveException() {
        // given
        FeignException feignException = new FeignException.InternalServerError(
                "Remove error",
                Request.create(
                        Request.HttpMethod.DELETE,
                        "/proposal",
                        Collections.emptyMap(),
                        null,
                        StandardCharsets.UTF_8,
                        null
                ),
                null,
                null
        );
        doThrow(feignException).when(serverAppClient).removeProposal(proposalUuid);
        // when & then
        ProposalRemoveException exception = assertThrows(ProposalRemoveException.class,
                () -> proposalStorageService.removeProposal(proposalUuid));
        assertTrue(exception.getMessage().contains("Failed to remove proposal"));
        verify(serverAppClient).removeProposal(proposalUuid);
    }
}
