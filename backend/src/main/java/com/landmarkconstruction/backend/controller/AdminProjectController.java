package com.landmarkconstruction.backend.controller;

import com.landmarkconstruction.backend.dto.common.PageResponse;
import com.landmarkconstruction.backend.dto.project.ImageReorderRequest;
import com.landmarkconstruction.backend.dto.project.ProjectCreateUpdateRequest;
import com.landmarkconstruction.backend.dto.project.ProjectDetailDto;
import com.landmarkconstruction.backend.dto.project.ProjectSummaryDto;
import com.landmarkconstruction.backend.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/projects")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProjectController {

    private final ProjectService projectService;

    @GetMapping
    public PageResponse<ProjectSummaryDto> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return PageResponse.of(projectService.listAllForAdmin(pageable));
    }

    @GetMapping("/{id}")
    public ProjectDetailDto get(@PathVariable Long id) {
        return projectService.getDetail(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectDetailDto create(@Valid @RequestBody ProjectCreateUpdateRequest request) {
        return projectService.create(request);
    }

    @PutMapping("/{id}")
    public ProjectDetailDto update(@PathVariable Long id, @Valid @RequestBody ProjectCreateUpdateRequest request) {
        return projectService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        projectService.delete(id);
    }

    @PostMapping(value = "/{id}/images", consumes = "multipart/form-data")
    public ProjectDetailDto addImages(@PathVariable Long id, @RequestParam("files") List<MultipartFile> files) {
        return projectService.addImages(id, files);
    }

    @PutMapping(value = "/{id}/cover-image", consumes = "multipart/form-data")
    public ProjectDetailDto setCoverImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return projectService.setCoverImage(id, file);
    }

    @PutMapping("/{id}/images/reorder")
    public ProjectDetailDto reorderImages(@PathVariable Long id, @Valid @RequestBody ImageReorderRequest request) {
        return projectService.reorderImages(id, request.imageIds());
    }

    @DeleteMapping("/{id}/images/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteImage(@PathVariable Long id, @PathVariable Long imageId) {
        projectService.deleteImage(id, imageId);
    }
}
