package com.driver_app.mappings;

import com.driver_app.model.Car;
import com.uber.common.dto.CarDto;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Component;

@Component
public class CarToCarDtoConverter implements Converter<Car, CarDto> {

    @Override
    public CarDto convert(MappingContext<Car, CarDto> context) {
        Car source = context.getSource();
        return new CarDto(
                source.getUuid(),
                source.getMake(),
                source.getModel(),
                source.getLicensePlate(),
                source.getCategory(),
                source.isActive()
        );
    }
}