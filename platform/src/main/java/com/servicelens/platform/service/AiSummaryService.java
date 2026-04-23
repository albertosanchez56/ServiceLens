package com.servicelens.platform.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.servicelens.platform.domain.Incident;
import com.servicelens.platform.domain.IncidentEvent;
import com.servicelens.platform.domain.IncidentEventType;

/**
 * Genera un resumen determinista (sin proveedor externo) a partir del incidente y su timeline.
 * El resultado está pensado para estudiarlo/demorarlo: incluye "citas" que apuntan a eventos/payload existentes.
 */
@Service
public class AiSummaryService {

    public ObjectNode buildSummaryPayload(Incident incident, List<IncidentEvent> events) {
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        payload.put("incidentId", incident.getId().toString());
        payload.put("generatedAt", Instant.now().toString());
        payload.put("mode", "DETERMINISTIC");

        ObjectNode incidentNode = payload.putObject("incident");
        incidentNode.put("title", incident.getTitle());
        incidentNode.put("state", incident.getState().name());
        incidentNode.put("severity", incident.getSeverity().name());
        incidentNode.put("rootService", incident.getRootService());
        incidentNode.put("startedAt", incident.getStartedAt().toString());
        incidentNode.put("updatedAt", incident.getUpdatedAt().toString());
        if (incident.getResolvedAt() != null) {
            incidentNode.put("resolvedAt", incident.getResolvedAt().toString());
        }

        ArrayNode citations = payload.putArray("citations");
        ArrayNode highlights = payload.putArray("highlights");

        // Última alerta disparada (lo más útil para una demo de detección).
        Optional<IncidentEvent> lastAlert = events.stream()
                .filter(e -> e.getEventType() == IncidentEventType.ALERT_TRIGGERED)
                .max(Comparator.comparing(IncidentEvent::getOccurredAt));

        if (lastAlert.isPresent()) {
            IncidentEvent ev = lastAlert.get();
            JsonNode p = ev.getPayload();
            String ruleName = text(p, "ruleName").orElse("regla-desconocida");
            String metricKey = text(p, "metricKey").orElse("METRIC");
            String comparison = text(p, "comparison").orElse("?");
            String threshold = text(p, "threshold").orElse("?");
            String observed = text(p, "observedValue").orElse("?");
            String promql = text(p, "prometheusQuery").orElse("");

            highlights.add("Se disparó una alerta por la regla \"" + ruleName + "\" (" + metricKey + ").");
            highlights.add("Comparación: observed " + comparison + " threshold (" + observed + " " + comparison + " "
                    + threshold + ").");
            if (!promql.isBlank()) {
                highlights.add("Consulta Prometheus usada: " + promql);
            }

            ObjectNode c = citations.addObject();
            c.put("type", "INCIDENT_EVENT");
            c.put("eventId", ev.getId().toString());
            c.put("eventType", ev.getEventType().name());
            c.put("note", "Fuente principal: ALERT_TRIGGERED con observed/threshold y promql.");
        } else {
            highlights.add("No hay evento ALERT_TRIGGERED en el timeline; el resumen se basa en metadatos del incidente.");
        }

        // Evidencia añadida por el usuario (si existe).
        long evidenceAdded = events.stream().filter(e -> e.getEventType() == IncidentEventType.EVIDENCE_ADDED).count();
        if (evidenceAdded > 0) {
            highlights.add("Hay " + evidenceAdded + " evidencias añadidas (EVIDENCE_ADDED) para ampliar la investigación.");
            ObjectNode c = citations.addObject();
            c.put("type", "TIMELINE_STAT");
            c.put("eventType", IncidentEventType.EVIDENCE_ADDED.name());
            c.put("count", evidenceAdded);
            c.put("note", "Conteo de eventos EVIDENCE_ADDED en el timeline.");
        }

        payload.put("summary", buildSummaryText(incident, highlights));
        return payload;
    }

    private static String buildSummaryText(Incident incident, ArrayNode highlights) {
        StringBuilder sb = new StringBuilder();
        sb.append("Incidente \"").append(incident.getTitle()).append("\" (")
                .append(incident.getSeverity().name()).append(") en ")
                .append(incident.getRootService()).append(". ");
        for (JsonNode h : highlights) {
            if (h != null && h.isTextual()) {
                sb.append(h.asText()).append(" ");
            }
        }
        return sb.toString().trim();
    }

    private static Optional<String> text(JsonNode node, String field) {
        if (node == null) {
            return Optional.empty();
        }
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) {
            return Optional.empty();
        }
        if (v.isTextual()) {
            return Optional.of(v.asText());
        }
        if (v.isNumber() || v.isBoolean()) {
            return Optional.of(v.asText());
        }
        return Optional.of(v.toString());
    }
}

