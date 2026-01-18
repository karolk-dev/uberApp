package com.driver_app.config;

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


}