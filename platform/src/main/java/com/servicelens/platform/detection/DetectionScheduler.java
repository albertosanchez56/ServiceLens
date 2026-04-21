package com.servicelens.platform.detection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.servicelens.platform.domain.DetectionRule;
import com.servicelens.platform.repo.DetectionRuleRepository;

@Component
@ConditionalOnProperty(name = "servicelens.detection.enabled", havingValue = "true", matchIfMissing = true)
public class DetectionScheduler {

    private static final Logger log = LoggerFactory.getLogger(DetectionScheduler.class);

    private final DetectionRuleRepository detectionRuleRepository;
    private final DetectionEvaluationService detectionEvaluationService;

    public DetectionScheduler(
            DetectionRuleRepository detectionRuleRepository,
            DetectionEvaluationService detectionEvaluationService) {
        this.detectionRuleRepository = detectionRuleRepository;
        this.detectionEvaluationService = detectionEvaluationService;
    }

    @Scheduled(fixedDelayString = "${servicelens.detection.poll-interval-ms:30000}")
    public void evaluateEnabledRules() {
        for (DetectionRule rule : detectionRuleRepository.findByEnabledTrue()) {
            try {
                detectionEvaluationService.evaluate(rule.getId());
            } catch (Exception e) {
                log.warn("Fallo evaluando regla {}: {}", rule.getId(), e.getMessage());
            }
        }
    }
}
