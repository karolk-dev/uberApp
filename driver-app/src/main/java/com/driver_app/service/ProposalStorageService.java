package com.driver_app.service;

import com.driver_app.clients.ServerAppClient;
import com.driver_app.exceptions.ProposalRemoveException;
import com.driver_app.exceptions.ProposalRetrieveException;
import com.driver_app.exceptions.ProposalStoreException;
import com.uber.common.dto.RideProposalDto;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProposalStorageService {
    private final ServerAppClient serverAppClient;

    public void storeProposal(RideProposalDto rideProposalDto) {
        try {
            serverAppClient.storeProposal(rideProposalDto);
        } catch (FeignException e) {
            log.error("Error storing proposal: {}", e.getMessage());
            throw new ProposalStoreException("Failed to store proposal", e);
        }
    }

    public Optional<RideProposalDto> getProposalForDriver(String uuid) {
        try {
            return Optional.ofNullable(serverAppClient.getProposal(uuid).getBody());
        } catch (FeignException.NotFound e) {
            return Optional.empty();
        } catch (FeignException e) {
            log.error("Error getting proposal: {}", e.getMessage());
            throw new ProposalRetrieveException("Failed to get proposal", e);
        }
    }

    public void removeProposal(String uuid) {
        try {
            serverAppClient.removeProposal(uuid);
        } catch (FeignException e) {
            log.error("Error removing proposal: {}", e.getMessage());
            throw new ProposalRemoveException("Failed to remove proposal", e);
        }
    }
}
