package com.uber.common.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@Builder
public class ClientDto {
    private Long id;
    private String uuid;
    private String username;
    private String email;
}