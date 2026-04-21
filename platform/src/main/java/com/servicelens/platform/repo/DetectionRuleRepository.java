package com.servicelens.platform.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.servicelens.platform.domain.DetectionRule;

public interface DetectionRuleRepository extends JpaRepository<DetectionRule, UUID> {

    List<DetectionRule> findByEnabledTrue();

    Page<DetectionRule> findByEnabled(boolean enabled, Pageable pageable);
}
