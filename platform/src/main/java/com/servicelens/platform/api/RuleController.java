package com.servicelens.platform.api;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.servicelens.platform.api.dto.PageResponse;
import com.servicelens.platform.api.dto.PatchRuleRequest;
import com.servicelens.platform.api.dto.RuleDetailDto;
import com.servicelens.platform.api.dto.RuleEvaluationPreviewDto;
import com.servicelens.platform.api.dto.RuleSummaryDto;
import com.servicelens.platform.service.RuleService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/rules")
public class RuleController {

    private final RuleService ruleService;

    public RuleController(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    @GetMapping
    public PageResponse<RuleSummaryDto> list(
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ruleService.list(enabled, page, size);
    }

    @GetMapping("/{id}")
    public RuleDetailDto get(@PathVariable UUID id) {
        return ruleService.get(id);
    }

    @PatchMapping("/{id}")
    public RuleDetailDto patch(@PathVariable UUID id, @Valid @RequestBody PatchRuleRequest req) {
        return ruleService.patch(id, req);
    }

    @GetMapping("/{id}/preview")
    public RuleEvaluationPreviewDto preview(@PathVariable UUID id) {
        return ruleService.previewEvaluation(id);
    }

    @PostMapping("/{id}/evaluate")
    public ResponseEntity<Void> evaluate(@PathVariable UUID id) {
        ruleService.runEvaluation(id);
        return ResponseEntity.accepted().build();
    }
}
