package com.servicelens.platform.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.servicelens.platform.api.dto.CreateEvidenceRequest;
import com.servicelens.platform.api.dto.CreateIncidentEventRequest;
import com.servicelens.platform.api.dto.CreateIncidentRequest;
import com.servicelens.platform.api.dto.EvidenceResponse;
import com.servicelens.platform.api.dto.IncidentDetailDto;
import com.servicelens.platform.api.dto.IncidentEventResponse;
import com.servicelens.platform.api.dto.IncidentSummaryDto;
import com.servicelens.platform.api.dto.PageResponse;
import com.servicelens.platform.api.dto.PatchIncidentRequest;
import com.servicelens.platform.domain.EvidenceRef;
import com.servicelens.platform.domain.Incident;
import com.servicelens.platform.domain.IncidentEvent;
import com.servicelens.platform.domain.IncidentEventType;
import com.servicelens.platform.domain.IncidentSeverity;
import com.servicelens.platform.domain.IncidentState;
import com.servicelens.platform.repo.EvidenceRefRepository;
import com.servicelens.platform.repo.IncidentEventRepository;
import com.servicelens.platform.repo.IncidentRepository;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final IncidentEventRepository incidentEventRepository;
    private final EvidenceRefRepository evidenceRefRepository;

    public IncidentService(
            IncidentRepository incidentRepository,
            IncidentEventRepository incidentEventRepository,
            EvidenceRefRepository evidenceRefRepository) {
        this.incidentRepository = incidentRepository;
        this.incidentEventRepository = incidentEventRepository;
        this.evidenceRefRepository = evidenceRefRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<IncidentSummaryDto> list(
            IncidentState state,
            IncidentSeverity severity,
            Instant from,
            Instant to,
            String q,
            int page,
            int size) {
        Specification<Incident> spec = IncidentSpecifications.withFilters(state, severity, from, to, q);
        Page<Incident> p = incidentRepository.findAll(
                spec,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt")));
        List<IncidentSummaryDto> items = new ArrayList<>();
        for (Incident i : p.getContent()) {
            long eventCount = incidentEventRepository.countByIncident_Id(i.getId());
            long signalCount = incidentEventRepository.countByIncident_IdAndEventType(i.getId(),
                    IncidentEventType.SIGNAL_LINKED)
                    + incidentEventRepository.countByIncident_IdAndEventType(i.getId(),
                            IncidentEventType.ALERT_TRIGGERED);
            items.add(new IncidentSummaryDto(
                    i.getId(),
                    i.getTitle(),
                    i.getState(),
                    i.getSeverity(),
                    i.getStartedAt(),
                    i.getUpdatedAt(),
                    i.getRootService(),
                    signalCount,
                    eventCount));
        }
        return new PageResponse<>(items, p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
    }

    @Transactional(readOnly = true)
    public IncidentDetailDto getById(UUID id) {
        Incident i = incidentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("INCIDENT_NOT_FOUND", "Incidente no encontrado"));
        return toDetail(i);
    }

    @Transactional
    public IncidentDetailDto create(CreateIncidentRequest req) {
        Instant started = req.startedAt() != null ? req.startedAt() : Instant.now();
        UUID id = UUID.randomUUID();
        Incident incident = new Incident(id, req.title(), req.severity(), started, req.rootService(), req.metadata());
        incident = incidentRepository.save(incident);
        return toDetail(incident);
    }

    @Transactional
    public IncidentDetailDto patch(UUID id, PatchIncidentRequest req) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("INCIDENT_NOT_FOUND", "Incidente no encontrado"));
        if (req.expectedVersion() != null && req.expectedVersion() != incident.getVersion()) {
            throw new ConflictException("VERSION_CONFLICT", "Versión obsoleta");
        }
        IncidentState oldState = incident.getState();
        if (req.title() != null) {
            incident.setTitle(req.title());
        }
        if (req.severity() != null) {
            incident.setSeverity(req.severity());
        }
        if (req.rootService() != null) {
            incident.setRootService(req.rootService());
        }
        if (req.metadata() != null) {
            incident.setMetadata(req.metadata());
        }
        if (req.state() != null) {
            if (!isValidTransition(oldState, req.state())) {
                throw new BusinessRuleException("INVALID_STATE_TRANSITION", "Transición de estado no permitida");
            }
            incident.setState(req.state());
            if (req.state() == IncidentState.RESOLVED && incident.getResolvedAt() == null) {
                incident.setResolvedAt(Instant.now());
            }
            addSystemEvent(incident, IncidentEventType.STATUS_CHANGE, statusPayload(oldState, req.state()));
        }
        incident.setUpdatedAt(Instant.now());
        try {
            incident = incidentRepository.save(incident);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new ConflictException("VERSION_CONFLICT", "Conflicto de versión");
        }
        return toDetail(incident);
    }

    private static JsonNode statusPayload(IncidentState from, IncidentState to) {
        ObjectNode n = JsonNodeFactory.instance.objectNode();
        n.put("from", from.name());
        n.put("to", to.name());
        return n;
    }

    private static boolean isValidTransition(IncidentState from, IncidentState to) {
        if (from == to) {
            return true;
        }
        return switch (to) {
            case OPEN -> from == IncidentState.ACK;
            case ACK -> from == IncidentState.OPEN;
            case RESOLVED -> from == IncidentState.OPEN || from == IncidentState.ACK;
        };
    }

    private static IncidentDetailDto toDetail(Incident i) {
        return new IncidentDetailDto(
                i.getId(),
                i.getTitle(),
                i.getState(),
                i.getSeverity(),
                i.getStartedAt(),
                i.getUpdatedAt(),
                i.getResolvedAt(),
                i.getRootService(),
                i.getSummary(),
                i.getMetadata(),
                i.getVersion());
    }

    @Transactional(readOnly = true)
    public List<IncidentEventResponse> listEvents(UUID incidentId) {
        ensureIncidentExists(incidentId);
        return incidentEventRepository.findByIncident_IdOrderByOccurredAtAsc(incidentId).stream()
                .map(this::toEventResponse)
                .toList();
    }

    @Transactional
    public IncidentEventResponse addEvent(UUID incidentId, CreateIncidentEventRequest req, String username) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new NotFoundException("INCIDENT_NOT_FOUND", "Incidente no encontrado"));
        if (req.type() == IncidentEventType.IA_SUMMARY) {
            throw new BusinessRuleException("BUSINESS_RULE_VIOLATION", "IA_SUMMARY solo por sistema");
        }
        UUID eid = UUID.randomUUID();
        boolean system = req.type() == IncidentEventType.ALERT_TRIGGERED || req.type() == IncidentEventType.SIGNAL_LINKED;
        IncidentEvent ev = new IncidentEvent(
                eid,
                incident,
                req.type(),
                Instant.now(),
                system ? null : username,
                system,
                req.payload());
        incidentEventRepository.save(ev);
        incident.setUpdatedAt(Instant.now());
        incidentRepository.save(incident);
        return toEventResponse(ev);
    }

    @Transactional(readOnly = true)
    public List<EvidenceResponse> listEvidence(UUID incidentId) {
        ensureIncidentExists(incidentId);
        return evidenceRefRepository.findByIncident_IdOrderByCreatedAtAsc(incidentId).stream()
                .map(this::toEvidenceResponse)
                .toList();
    }

    @Transactional
    public EvidenceResponse addEvidence(UUID incidentId, CreateEvidenceRequest req, String username) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new NotFoundException("INCIDENT_NOT_FOUND", "Incidente no encontrado"));
        UUID eid = UUID.randomUUID();
        EvidenceRef ev = new EvidenceRef(
                eid,
                incident,
                req.type(),
                req.label(),
                req.ref(),
                req.metadata(),
                Instant.now());
        evidenceRefRepository.save(ev);
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        payload.put("evidenceId", eid.toString());
        IncidentEvent note = new IncidentEvent(
                UUID.randomUUID(),
                incident,
                IncidentEventType.EVIDENCE_ADDED,
                Instant.now(),
                username,
                false,
                payload);
        incidentEventRepository.save(note);
        incident.setUpdatedAt(Instant.now());
        incidentRepository.save(incident);
        return toEvidenceResponse(ev);
    }

    private void addSystemEvent(Incident incident, IncidentEventType type, JsonNode payload) {
        IncidentEvent ev = new IncidentEvent(
                UUID.randomUUID(),
                incident,
                type,
                Instant.now(),
                null,
                true,
                payload);
        incidentEventRepository.save(ev);
    }

    private void ensureIncidentExists(UUID id) {
        if (!incidentRepository.existsById(id)) {
            throw new NotFoundException("INCIDENT_NOT_FOUND", "Incidente no encontrado");
        }
    }

    private IncidentEventResponse toEventResponse(IncidentEvent e) {
        return new IncidentEventResponse(
                e.getId(),
                e.getIncident().getId(),
                e.getEventType(),
                e.getOccurredAt(),
                new IncidentEventResponse.ActorDto(
                        e.getActorUsername(),
                        e.isSystemActor()),
                e.getPayload());
    }

    private EvidenceResponse toEvidenceResponse(EvidenceRef e) {
        return new EvidenceResponse(
                e.getId(),
                e.getEvidenceType(),
                e.getLabel(),
                e.getRefText(),
                e.getMetadata(),
                e.getCreatedAt());
    }
}
