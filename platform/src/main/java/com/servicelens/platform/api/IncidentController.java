package com.servicelens.platform.api;

import java.time.Instant;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.servicelens.platform.api.dto.CreateIncidentRequest;
import com.servicelens.platform.api.dto.IncidentDetailDto;
import com.servicelens.platform.api.dto.IncidentEventResponse;
import com.servicelens.platform.api.dto.IncidentSummaryDto;
import com.servicelens.platform.api.dto.PageResponse;
import com.servicelens.platform.api.dto.PatchIncidentRequest;
import com.servicelens.platform.domain.IncidentSeverity;
import com.servicelens.platform.domain.IncidentState;
import com.servicelens.platform.service.IncidentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping
    public PageResponse<IncidentSummaryDto> list(
            @RequestParam(required = false) IncidentState state,
            @RequestParam(required = false) IncidentSeverity severity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return incidentService.list(state, severity, from, to, q, page, size);
    }

    @GetMapping("/{id}")
    public IncidentDetailDto get(@PathVariable UUID id) {
        return incidentService.getById(id);
    }

    @PostMapping
    public ResponseEntity<IncidentDetailDto> create(@Valid @RequestBody CreateIncidentRequest req) {
        IncidentDetailDto body = incidentService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "/api/v1/incidents/" + body.id())
                .body(body);
    }

    @PatchMapping("/{id}")
    public IncidentDetailDto patch(@PathVariable UUID id, @Valid @RequestBody PatchIncidentRequest req) {
        return incidentService.patch(id, req);
    }

    @PostMapping("/{id}/ai-summary")
    public IncidentEventResponse aiSummary(@PathVariable UUID id) {
        return incidentService.generateAiSummary(id);
    }
}
