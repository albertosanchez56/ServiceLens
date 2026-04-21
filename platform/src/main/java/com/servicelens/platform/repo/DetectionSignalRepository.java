package com.servicelens.platform.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.servicelens.platform.domain.DetectionSignal;

public interface DetectionSignalRepository extends JpaRepository<DetectionSignal, UUID>,
        JpaSpecificationExecutor<DetectionSignal> {

    Optional<DetectionSignal> findByRule_IdAndFingerprint(UUID ruleId, String fingerprint);
}
