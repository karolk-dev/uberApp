package com.server_app.config;

import com.netflix.discovery.shared.transport.jersey3.Jersey3TransportClientFactories;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class ApplicationConfig {

    @Bean
    public ModelMapper modelMapper(Set<Converter> converter) {
        ModelMapper modelMapper = new ModelMapper();
        converter.forEach(modelMapper::addConverter);
        return modelMapper;
    }

//    @Bean
//    public FlywayMigrationStrategy cleanMigrateStrategy() {
//        return flyway -> {
//            flyway.repair();
//            flyway.migrate();
//        };
//    }

    @Bean
    public Jersey3TransportClientFactories jersey3TransportClientFactories() {
        return new Jersey3TransportClientFactories();
    }
}
