package com.driver_app.service;

import com.uber.common.dto.DriverResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResponseSenderService {
    private final KafkaTemplate<String, DriverResponseDto> kafkaTemplate;

    public void sendResponse(String uuid, boolean accepted, String rideUuid) {
        DriverResponseDto driverResponseDto = DriverResponseDto.builder()
                .driverUuid(uuid)
                .accepted(accepted)
                .rideUuid(rideUuid)
                .build();

        kafkaTemplate.send("driver_responses", driverResponseDto);
    }
}
