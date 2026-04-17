package com.servicelens.platform.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class MetaController {

    @GetMapping("/meta")
    public MetaResponse meta() {
        return new MetaResponse("1.0.0-mvp", false, false);
    }

    public record MetaResponse(String apiVersion, boolean aiEnabled, boolean ragEnabled) {
    }
}
