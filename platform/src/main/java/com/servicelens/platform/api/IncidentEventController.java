package com.servicelens.platform.api;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.servicelens.platform.api.dto.CreateIncidentEventRequest;
import com.servicelens.platform.api.dto.IncidentEventResponse;
import com.servicelens.platform.service.IncidentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/incidents/{incidentId}/events")
public class IncidentEventController {

    private final IncidentService incidentService;

    public IncidentEventController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping
    public List<IncidentEventResponse> list(@PathVariable UUID incidentId) {
        return incidentService.listEvents(incidentId);
    }

    @PostMapping
    public IncidentEventResponse add(
            @PathVariable UUID incidentId,
            @Valid @RequestBody CreateIncidentEventRequest req,
            Authentication authentication) {
        String user = authentication != null ? authentication.getName() : "unknown";
        return incidentService.addEvent(incidentId, req, user);
    }
}
