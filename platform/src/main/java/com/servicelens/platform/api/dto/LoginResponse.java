package com.servicelens.platform.api.dto;

import java.util.List;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        List<String> roles
) {
}
