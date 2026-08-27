package com.landmarkconstruction.backend.repository;

import com.landmarkconstruction.backend.model.Project;
import com.landmarkconstruction.backend.model.enums.ProjectCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Page<Project> findByPublishedTrueAndCategory(ProjectCategory category, Pageable pageable);

    Page<Project> findByPublishedTrue(Pageable pageable);
}
