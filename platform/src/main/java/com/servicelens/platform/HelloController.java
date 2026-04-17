package com.servicelens.platform;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/api/v1/meta")
    public MetaResponse meta() {
        return new MetaResponse("1.0.0-mvp", false, false);
    }

    public record MetaResponse(String apiVersion, boolean aiEnabled, boolean ragEnabled) {}
}
