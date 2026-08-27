package com.landmarkconstruction.backend.repository;

import com.landmarkconstruction.backend.model.QuoteRequest;
import com.landmarkconstruction.backend.model.enums.QuoteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuoteRequestRepository extends JpaRepository<QuoteRequest, Long> {
    Page<QuoteRequest> findByStatus(QuoteStatus status, Pageable pageable);
}
