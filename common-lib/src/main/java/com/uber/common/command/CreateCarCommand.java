package com.uber.common.command;

import com.uber.common.model.CarCategory;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateCarCommand {
    @NotBlank(message = "Marka pojazdu jest wymagana")
    private String make;

    @NotBlank(message = "Model pojazdu jest wymagany")
    private String model;

    @NotBlank(message = "Numer rejestracyjny jest wymagany")
    private String licensePlate;

    @NotNull(message = "Kategoria pojazdu jest wymagana")
    @Enumerated(EnumType.STRING)
    private CarCategory category;
}
