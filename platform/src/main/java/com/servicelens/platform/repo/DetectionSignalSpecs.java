package com.servicelens.platform.repo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.servicelens.platform.domain.DetectionSignal;
import com.servicelens.platform.domain.SignalStatus;

import jakarta.persistence.criteria.Predicate;

public final class DetectionSignalSpecs {

    private DetectionSignalSpecs() {
    }

    public static Specification<DetectionSignal> withFilters(UUID ruleId, UUID incidentId, SignalStatus status) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (ruleId != null) {
                p.add(cb.equal(root.get("rule").get("id"), ruleId));
            }
            if (incidentId != null) {
                p.add(cb.equal(root.get("incident").get("id"), incidentId));
            }
            if (status != null) {
                p.add(cb.equal(root.get("status"), status));
            }
            return p.isEmpty() ? cb.conjunction() : cb.and(p.toArray(Predicate[]::new));
        };
    }
}
