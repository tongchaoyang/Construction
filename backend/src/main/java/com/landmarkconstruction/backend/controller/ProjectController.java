package com.landmarkconstruction.backend.controller;

import com.landmarkconstruction.backend.dto.common.PageResponse;
import com.landmarkconstruction.backend.dto.project.ProjectDetailDto;
import com.landmarkconstruction.backend.dto.project.ProjectSummaryDto;
import com.landmarkconstruction.backend.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public PageResponse<ProjectSummaryDto> list(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return PageResponse.of(projectService.listPublished(category, pageable));
    }

    @GetMapping("/{id}")
    public ProjectDetailDto get(@PathVariable Long id) {
        return projectService.getDetail(id);
    }
}
