package com.servicelens.platform.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.servicelens.platform.domain.Incident;
import com.servicelens.platform.domain.IncidentSeverity;
import com.servicelens.platform.domain.IncidentState;

import jakarta.persistence.criteria.Predicate;

public final class IncidentSpecifications {

    private IncidentSpecifications() {
    }

    public static Specification<Incident> withFilters(
            IncidentState state,
            IncidentSeverity severity,
            Instant from,
            Instant to,
            String q) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (state != null) {
                p.add(cb.equal(root.get("state"), state));
            }
            if (severity != null) {
                p.add(cb.equal(root.get("severity"), severity));
            }
            if (from != null) {
                p.add(cb.greaterThanOrEqualTo(root.get("updatedAt"), from));
            }
            if (to != null) {
                p.add(cb.lessThanOrEqualTo(root.get("updatedAt"), to));
            }
            if (q != null && !q.isBlank()) {
                p.add(cb.like(cb.lower(root.get("title")), "%" + q.toLowerCase() + "%"));
            }
            return p.isEmpty() ? cb.conjunction() : cb.and(p.toArray(new Predicate[0]));
        };
    }
}
