package com.servicelens.platform.api;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.servicelens.platform.api.dto.MeResponse;

@RestController
@RequestMapping("/api/v1")
public class MeController {

    @GetMapping("/me")
    public MeResponse me(Authentication authentication) {
        String username = authentication.getName();
        UUID userId = UUID.nameUUIDFromBytes(username.getBytes(StandardCharsets.UTF_8));
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.replace("ROLE_", ""))
                .toList();
        return new MeResponse(userId, username, roles);
    }
}
