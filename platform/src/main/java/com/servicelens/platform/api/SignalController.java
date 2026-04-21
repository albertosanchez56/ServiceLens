package com.servicelens.platform.api;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.servicelens.platform.api.dto.PageResponse;
import com.servicelens.platform.api.dto.PatchSignalRequest;
import com.servicelens.platform.api.dto.SignalDto;
import com.servicelens.platform.domain.SignalStatus;
import com.servicelens.platform.service.SignalService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/signals")
public class SignalController {

    private final SignalService signalService;

    public SignalController(SignalService signalService) {
        this.signalService = signalService;
    }

    @GetMapping
    public PageResponse<SignalDto> list(
            @RequestParam(required = false) UUID ruleId,
            @RequestParam(required = false) UUID incidentId,
            @RequestParam(required = false) SignalStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return signalService.list(ruleId, incidentId, status, page, size);
    }

    @GetMapping("/{id}")
    public SignalDto get(@PathVariable UUID id) {
        return signalService.get(id);
    }

    @PatchMapping("/{id}")
    public SignalDto patch(@PathVariable UUID id, @Valid @RequestBody PatchSignalRequest req) {
        return signalService.patch(id, req);
    }
}
