package com.servicelens.platform.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.servicelens.platform.domain.EvidenceRef;

public interface EvidenceRefRepository extends JpaRepository<EvidenceRef, UUID> {

    List<EvidenceRef> findByIncident_IdOrderByCreatedAtAsc(UUID incidentId);
}
