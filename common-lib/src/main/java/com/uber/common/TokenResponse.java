package com.uber.common;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TokenResponse {
    private String access_token;
    private String refresh_token;
    private String token_type;
    private Long expires_in;
}
