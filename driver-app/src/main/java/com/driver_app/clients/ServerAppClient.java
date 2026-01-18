package com.driver_app.clients;


import com.uber.common.dto.RideProposalDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "server-app", path = "/api/proposals")
public interface ServerAppClient {

    @PostMapping
    ResponseEntity<Void> storeProposal(@RequestBody RideProposalDto proposal);

    @GetMapping("/{driverUuid}")
    ResponseEntity<RideProposalDto> getProposal(@PathVariable("driverUuid") String driverUuid);

    @DeleteMapping("/{driverUuid}")
    ResponseEntity<Void> removeProposal(@PathVariable("driverUuid") String driverUuid);
}
