package com.servicelens.platform.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.servicelens.platform.api.dto.PageResponse;
import com.servicelens.platform.api.dto.PatchSignalRequest;
import com.servicelens.platform.api.dto.SignalDto;
import com.servicelens.platform.domain.DetectionSignal;
import com.servicelens.platform.domain.SignalStatus;
import com.servicelens.platform.repo.DetectionSignalRepository;
import com.servicelens.platform.repo.DetectionSignalSpecs;

@Service
public class SignalService {

    private final DetectionSignalRepository signalRepository;

    public SignalService(DetectionSignalRepository signalRepository) {
        this.signalRepository = signalRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<SignalDto> list(UUID ruleId, UUID incidentId, SignalStatus status, int page, int size) {
        Specification<DetectionSignal> spec = DetectionSignalSpecs.withFilters(ruleId, incidentId, status);
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "occurredAt"));
        Page<DetectionSignal> pg = signalRepository.findAll(spec, pageable);
        List<SignalDto> items = new ArrayList<>();
        for (DetectionSignal s : pg.getContent()) {
            items.add(toDto(s));
        }
        return new PageResponse<>(items, pg.getNumber(), pg.getSize(), pg.getTotalElements(), pg.getTotalPages());
    }

    @Transactional(readOnly = true)
    public SignalDto get(UUID id) {
        DetectionSignal s = signalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("SIGNAL_NOT_FOUND", "Señal no encontrada"));
        return toDto(s);
    }

    @Transactional
    public SignalDto patch(UUID id, PatchSignalRequest req) {
        DetectionSignal s = signalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("SIGNAL_NOT_FOUND", "Señal no encontrada"));
        s.setStatus(req.status());
        s.setUpdatedAt(java.time.Instant.now());
        s = signalRepository.save(s);
        return toDto(s);
    }

    private static SignalDto toDto(DetectionSignal s) {
        return new SignalDto(
                s.getId(),
                s.getRule().getId(),
                s.getStatus().name(),
                s.getFingerprint(),
                s.getOccurredAt(),
                s.getIncident() != null ? s.getIncident().getId() : null,
                s.getPayload());
    }
}
