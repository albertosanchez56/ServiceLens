package com.servicelens.platform.api.dto;

import java.util.List;
import java.util.UUID;

public record MeResponse(
        UUID userId,
        String username,
        List<String> roles
) {
}
