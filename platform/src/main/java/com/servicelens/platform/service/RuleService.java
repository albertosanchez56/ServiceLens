package com.servicelens.platform.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.servicelens.platform.api.dto.PageResponse;
import com.servicelens.platform.api.dto.PatchRuleRequest;
import com.servicelens.platform.api.dto.RuleDetailDto;
import com.servicelens.platform.api.dto.RuleEvaluationPreviewDto;
import com.servicelens.platform.api.dto.RuleSummaryDto;
import com.servicelens.platform.detection.DetectionEvaluationService;
import com.servicelens.platform.detection.DetectionMath;
import com.servicelens.platform.detection.PrometheusClient;
import com.servicelens.platform.detection.PrometheusPromql;
import com.servicelens.platform.domain.DetectionRule;
import com.servicelens.platform.repo.DetectionRuleRepository;

@Service
public class RuleService {

    private final DetectionRuleRepository ruleRepository;
    private final DetectionEvaluationService detectionEvaluationService;
    private final PrometheusClient prometheusClient;
    private final String prometheusBaseUrl;

    public RuleService(
            DetectionRuleRepository ruleRepository,
            DetectionEvaluationService detectionEvaluationService,
            PrometheusClient prometheusClient,
            @Value("${servicelens.prometheus.base-url:http://localhost:9090}") String prometheusBaseUrl) {
        this.ruleRepository = ruleRepository;
        this.detectionEvaluationService = detectionEvaluationService;
        this.prometheusClient = prometheusClient;
        this.prometheusBaseUrl = prometheusBaseUrl;
    }

    public void runEvaluation(UUID id) {
        detectionEvaluationService.evaluate(id);
    }

    @Transactional(readOnly = true)
    public RuleEvaluationPreviewDto previewEvaluation(UUID id) {
        DetectionRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("RULE_NOT_FOUND", "Regla no encontrada"));
        String promql = PrometheusPromql.build(rule);
        OptionalDouble scalar = prometheusClient.queryScalar(promql);
        boolean empty = scalar.isEmpty();
        double observed = scalar.orElse(Double.NaN);
        boolean breach = !empty && DetectionMath.isBreach(scalar.getAsDouble(), rule);
        return new RuleEvaluationPreviewDto(
                prometheusBaseUrl,
                promql,
                empty,
                empty ? 0.0 : observed,
                breach,
                Double.toString(rule.getThreshold()),
                rule.getComparison().name());
    }

    @Transactional(readOnly = true)
    public PageResponse<RuleSummaryDto> list(Boolean enabled, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<DetectionRule> pg =
                enabled == null ? ruleRepository.findAll(pageable) : ruleRepository.findByEnabled(enabled, pageable);
        List<RuleSummaryDto> items = new ArrayList<>();
        for (DetectionRule r : pg.getContent()) {
            items.add(toSummary(r));
        }
        return new PageResponse<>(items, pg.getNumber(), pg.getSize(), pg.getTotalElements(), pg.getTotalPages());
    }

    @Transactional(readOnly = true)
    public RuleDetailDto get(UUID id) {
        DetectionRule r = ruleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("RULE_NOT_FOUND", "Regla no encontrada"));
        return toDetail(r);
    }

    @Transactional
    public RuleDetailDto patch(UUID id, PatchRuleRequest req) {
        DetectionRule r = ruleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("RULE_NOT_FOUND", "Regla no encontrada"));
        if (req.enabled() != null) {
            r.setEnabled(req.enabled());
        }
        if (req.threshold() != null) {
            r.setThreshold(req.threshold());
        }
        if (req.windowMinutes() != null) {
            r.setWindowMinutes(req.windowMinutes());
        }
        r.setUpdatedAt(Instant.now());
        r = ruleRepository.save(r);
        return toDetail(r);
    }

    private static RuleSummaryDto toSummary(DetectionRule r) {
        return new RuleSummaryDto(
                r.getId(),
                r.getName(),
                r.isEnabled(),
                r.getTargetService(),
                PrometheusPromql.conditionSummary(r),
                r.getCreatedAt(),
                r.getUpdatedAt());
    }

    private static RuleDetailDto toDetail(DetectionRule r) {
        return new RuleDetailDto(
                r.getId(),
                r.getName(),
                r.isEnabled(),
                r.getTargetService(),
                r.getMetricKey().name(),
                r.getThreshold(),
                r.getWindowMinutes(),
                r.getComparison().name(),
                r.getDedupeFingerprintTemplate(),
                r.getCreatedAt(),
                r.getUpdatedAt());
    }
}
