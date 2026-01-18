package com.client_app.model.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CreateClientCommand {
    @NotNull
    private String username;

    @Email
    @NotNull
    private String email;

    @NotNull
    private String password;
}