package com.uber.common.command;

import com.uber.common.Coordinates;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateDriverCommand {
    @NotBlank
    private String name;

    @NotBlank
    @Pattern(regexp = "\\d{10}")
    private String nip;

    @NotBlank
    private String password;
}