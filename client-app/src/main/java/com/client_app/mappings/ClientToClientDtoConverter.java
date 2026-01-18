package com.client_app.mappings;

import com.client_app.model.client.Client;
import com.uber.common.dto.ClientDto;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Component;

@Component
public class ClientToClientDtoConverter implements Converter<Client, ClientDto> {

    @Override
    public ClientDto convert(MappingContext<Client, ClientDto> context) {
        Client source = context.getSource();
        return new ClientDto(
                source.getId(),
                source.getUuid(),
                source.getUsername(),
                source.getEmail()
        );
    }
}
