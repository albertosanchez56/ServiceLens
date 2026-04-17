package com.servicelens.platform.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.servicelens.platform.domain.IncidentEvent;
import com.servicelens.platform.domain.IncidentEventType;

public interface IncidentEventRepository extends JpaRepository<IncidentEvent, UUID> {

    List<IncidentEvent> findByIncident_IdOrderByOccurredAtAsc(UUID incidentId);

    long countByIncident_Id(UUID incidentId);

    long countByIncident_IdAndEventType(UUID incidentId, IncidentEventType type);
}
