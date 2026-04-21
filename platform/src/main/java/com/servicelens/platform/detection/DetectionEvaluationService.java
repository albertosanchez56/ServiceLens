package com.servicelens.platform.detection;

import java.time.Instant;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.servicelens.platform.api.dto.IncidentDetailDto;
import com.servicelens.platform.domain.DetectionRule;
import com.servicelens.platform.domain.DetectionSignal;
import com.servicelens.platform.domain.Incident;
import com.servicelens.platform.domain.SignalStatus;
import com.servicelens.platform.repo.DetectionRuleRepository;
import com.servicelens.platform.repo.DetectionSignalRepository;
import com.servicelens.platform.repo.IncidentRepository;
import com.servicelens.platform.service.IncidentService;
import com.servicelens.platform.service.NotFoundException;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * Evalúa una regla frente a Prometheus, aplica deduplicación por ventana y abre incidentes.
 *
 * <p><strong>Deduplicación:</strong> fingerprint = {@code ruleId + ":" + windowBucketId} donde el bucket
 * es {@code floor(epochSeconds / (windowMinutes * 60))}. Si ya existe una señal con el mismo par
 * (rule, fingerprint), se actualiza el payload (valor observado, consulta) y la fecha de la señal/incidente
 * sin crear un nuevo incidente.</p>
 */
@Service
public class DetectionEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(DetectionEvaluationService.class);

    private final PrometheusClient prometheusClient;
    private final DetectionRuleRepository ruleRepository;
    private final DetectionSignalRepository signalRepository;
    private final IncidentRepository incidentRepository;
    private final IncidentService incidentService;
    private final MeterRegistry meterRegistry;
    private final ObjectMapper objectMapper;

    public DetectionEvaluationService(
            PrometheusClient prometheusClient,
            DetectionRuleRepository ruleRepository,
            DetectionSignalRepository signalRepository,
            IncidentRepository incidentRepository,
            IncidentService incidentService,
            MeterRegistry meterRegistry,
            ObjectMapper objectMapper) {
        this.prometheusClient = prometheusClient;
        this.ruleRepository = ruleRepository;
        this.signalRepository = signalRepository;
        this.incidentRepository = incidentRepository;
        this.incidentService = incidentService;
        this.meterRegistry = meterRegistry;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void evaluate(UUID ruleId) {
        DetectionRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new NotFoundException("RULE_NOT_FOUND", "Regla no encontrada"));
        if (!rule.isEnabled()) {
            return;
        }
        String promql = PrometheusPromql.build(rule);
        OptionalDouble scalar = prometheusClient.queryScalar(promql);
        meterRegistry.counter("servicelens.detection.evaluations").increment();

        if (scalar.isEmpty()) {
            log.warn(
                    "Detección: Prometheus no devolvió valor parseable (¿URL desde el contenedor?). Regla={} promql={}",
                    rule.getId(),
                    promql);
            return;
        }
        double observed = scalar.getAsDouble();

        Instant now = Instant.now();
        int wm = rule.getWindowMinutes();
        long bucket = DetectionMath.windowBucketId(now, wm);
        String fp = DetectionMath.fingerprint(rule, bucket);
        Instant windowStart = DetectionMath.windowStart(now, wm);
        Instant windowEnd = DetectionMath.windowEnd(windowStart, wm);

        if (!DetectionMath.isBreach(observed, rule)) {
            log.info(
                    "Detección: sin incumplimiento. Regla={} observed={} threshold={} {}",
                    rule.getId(),
                    observed,
                    rule.getThreshold(),
                    rule.getComparison());
            return;
        }

        Optional<DetectionSignal> existing = signalRepository.findByRule_IdAndFingerprint(rule.getId(), fp);
        if (existing.isPresent()) {
            dedupeUpdate(existing.get(), observed, promql, now, windowStart, windowEnd);
            meterRegistry.counter("servicelens.detection.signals_deduped").increment();
            return;
        }

        UUID signalId = UUID.randomUUID();
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("ruleId", rule.getId().toString());
        payload.put("signalId", signalId.toString());
        payload.put("fingerprint", fp);
        payload.put("observedValue", observed);
        payload.put("threshold", rule.getThreshold());
        payload.put("prometheusQuery", promql);
        payload.put("ruleName", rule.getName());
        payload.put("metricKey", rule.getMetricKey().name());
        payload.put("comparison", rule.getComparison().name());
        ObjectNode window = objectMapper.createObjectNode();
        window.put("from", windowStart.toString());
        window.put("to", windowEnd.toString());
        payload.set("window", window);

        IncidentDetailDto incidentDto =
                incidentService.createFromDetectionAlert("Alerta: " + rule.getName(), rule.getTargetService(), payload);
        Incident incident = incidentRepository.findById(incidentDto.id())
                .orElseThrow(() -> new IllegalStateException("Incidente recién creado no encontrado"));

        DetectionSignal signal = new DetectionSignal(
                signalId,
                rule,
                SignalStatus.OPEN,
                fp,
                now,
                incident,
                payload,
                windowStart,
                windowEnd,
                now,
                now);
        signalRepository.save(signal);
        meterRegistry.counter("servicelens.detection.signals_created").increment();
        meterRegistry.counter("servicelens.detection.incidents_opened").increment();
        log.info(
                "Detección: nueva señal e incidente para regla {} (servicio {}, valor {})",
                rule.getId(),
                rule.getTargetService(),
                observed);
    }

    private void dedupeUpdate(
            DetectionSignal signal,
            double observed,
            String promql,
            Instant now,
            Instant windowStart,
            Instant windowEnd) {
        ObjectNode p = objectMapper.convertValue(signal.getPayload(), ObjectNode.class);
        p.put("observedValue", observed);
        p.put("lastEvaluatedAt", now.toString());
        p.put("prometheusQuery", promql);
        ObjectNode window = objectMapper.createObjectNode();
        window.put("from", windowStart.toString());
        window.put("to", windowEnd.toString());
        p.set("window", window);
        signal.setPayload(p);
        signal.setOccurredAt(now);
        signal.setUpdatedAt(now);
        signal.setWindowStart(windowStart);
        signal.setWindowEnd(windowEnd);
        signalRepository.save(signal);
        if (signal.getIncident() != null) {
            Incident inc = signal.getIncident();
            inc.setUpdatedAt(now);
            incidentRepository.save(inc);
        }
    }
}
