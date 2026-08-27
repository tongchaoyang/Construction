package com.landmarkconstruction.backend.repository;

import com.landmarkconstruction.backend.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByProjectId(Long projectId, Pageable pageable);

    Optional<Review> findByProjectIdAndUserId(Long projectId, Long userId);

    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    @Query("select avg(r.rating) from Review r where r.project.id = :projectId")
    Double findAverageRatingByProjectId(Long projectId);

    long countByProjectId(Long projectId);
}
