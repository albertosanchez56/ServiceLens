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

import com.servicelens.platform.api.dto.CreateEvidenceRequest;
import com.servicelens.platform.api.dto.EvidenceResponse;
import com.servicelens.platform.service.IncidentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/incidents/{incidentId}/evidence")
public class EvidenceController {

    private final IncidentService incidentService;

    public EvidenceController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping
    public List<EvidenceResponse> list(@PathVariable UUID incidentId) {
        return incidentService.listEvidence(incidentId);
    }

    @PostMapping
    public EvidenceResponse add(
            @PathVariable UUID incidentId,
            @Valid @RequestBody CreateEvidenceRequest req,
            Authentication authentication) {
        String user = authentication != null ? authentication.getName() : "unknown";
        return incidentService.addEvidence(incidentId, req, user);
    }
}
