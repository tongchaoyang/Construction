package com.landmarkconstruction.backend.repository;

import com.landmarkconstruction.backend.model.ProjectImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectImageRepository extends JpaRepository<ProjectImage, Long> {
}
