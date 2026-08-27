package com.landmarkconstruction.backend.service;

import com.landmarkconstruction.backend.dto.project.*;
import com.landmarkconstruction.backend.exception.BadRequestException;
import com.landmarkconstruction.backend.exception.ResourceNotFoundException;
import com.landmarkconstruction.backend.model.Project;
import com.landmarkconstruction.backend.model.ProjectImage;
import com.landmarkconstruction.backend.model.enums.ProjectCategory;
import com.landmarkconstruction.backend.repository.ProjectImageRepository;
import com.landmarkconstruction.backend.repository.ProjectRepository;
import com.landmarkconstruction.backend.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectImageRepository projectImageRepository;
    private final ReviewRepository reviewRepository;
    private final StorageService storageService;

    @Transactional(readOnly = true)
    public Page<ProjectSummaryDto> listPublished(String categoryStr, Pageable pageable) {
        Page<Project> page;
        if (categoryStr != null && !categoryStr.isBlank()) {
            ProjectCategory category = parseCategory(categoryStr);
            page = projectRepository.findByPublishedTrueAndCategory(category, pageable);
        } else {
            page = projectRepository.findByPublishedTrue(pageable);
        }
        return page.map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public Page<ProjectSummaryDto> listAllForAdmin(Pageable pageable) {
        return projectRepository.findAll(pageable).map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public ProjectDetailDto getDetail(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
        return toDetail(project);
    }

    @Transactional
    public ProjectDetailDto create(ProjectCreateUpdateRequest request) {
        Project project = new Project();
        applyRequest(project, request);
        projectRepository.save(project);
        return toDetail(project);
    }

    @Transactional
    public ProjectDetailDto update(Long id, ProjectCreateUpdateRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
        applyRequest(project, request);
        return toDetail(project);
    }

    @Transactional
    public void delete(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
        project.getImages().forEach(img -> storageService.delete(img.getImageUrl()));
        if (project.getCoverImageUrl() != null) {
            storageService.delete(project.getCoverImageUrl());
        }
        projectRepository.delete(project);
    }

    @Transactional
    public ProjectDetailDto addImages(Long projectId, List<MultipartFile> files) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
        S3StorageService.validateBatch(files, 20);
        int nextOrder = project.getImages().size();
        for (MultipartFile file : files) {
            String url = storageService.upload(file, "projects/" + projectId);
            ProjectImage image = new ProjectImage();
            image.setProject(project);
            image.setImageUrl(url);
            image.setSortOrder(nextOrder++);
            project.getImages().add(image);
        }
        if (project.getCoverImageUrl() == null && !project.getImages().isEmpty()) {
            project.setCoverImageUrl(project.getImages().get(0).getImageUrl());
        }
        projectRepository.save(project);
        return toDetail(project);
    }

    @Transactional
    public ProjectDetailDto setCoverImage(Long projectId, MultipartFile file) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
        String url = storageService.upload(file, "projects/" + projectId + "/cover");
        project.setCoverImageUrl(url);
        projectRepository.save(project);
        return toDetail(project);
    }

    @Transactional
    public void deleteImage(Long projectId, Long imageId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
        ProjectImage image = project.getImages().stream()
                .filter(i -> i.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Image not found: " + imageId));
        storageService.delete(image.getImageUrl());
        project.getImages().remove(image);
        projectImageRepository.delete(image);
    }

    @Transactional
    public ProjectDetailDto reorderImages(Long projectId, List<Long> imageIdsInOrder) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
        Map<Long, ProjectImage> byId = project.getImages().stream()
                .collect(java.util.stream.Collectors.toMap(ProjectImage::getId, i -> i));
        int order = 0;
        for (Long imageId : imageIdsInOrder) {
            ProjectImage image = byId.get(imageId);
            if (image == null) {
                throw new BadRequestException("Image " + imageId + " does not belong to project " + projectId);
            }
            image.setSortOrder(order++);
        }
        projectRepository.save(project);
        return toDetail(project);
    }

    private void applyRequest(Project project, ProjectCreateUpdateRequest request) {
        project.setTitleEn(request.titleEn());
        project.setTitleZh(request.titleZh());
        project.setDescriptionEn(request.descriptionEn());
        project.setDescriptionZh(request.descriptionZh());
        project.setCategory(parseCategory(request.category()));
        project.setLocation(request.location());
        project.setCompletedDate(request.completedDate());
        if (request.published() != null) {
            project.setPublished(request.published());
        }
    }

    private ProjectCategory parseCategory(String value) {
        try {
            return ProjectCategory.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid project category: " + value);
        }
    }

    private ProjectSummaryDto toSummary(Project project) {
        double avg = averageRating(project.getId());
        long count = reviewRepository.countByProjectId(project.getId());
        return new ProjectSummaryDto(project.getId(), project.getTitleEn(), project.getTitleZh(),
                project.getCategory().name(), project.getLocation(), project.getCompletedDate(),
                project.getCoverImageUrl(), project.isPublished(), avg, count);
    }

    private ProjectDetailDto toDetail(Project project) {
        List<ProjectImageDto> images = project.getImagesOrdered().stream()
                .map(i -> new ProjectImageDto(i.getId(), i.getImageUrl(), i.getSortOrder()))
                .toList();
        double avg = averageRating(project.getId());
        long count = reviewRepository.countByProjectId(project.getId());
        return new ProjectDetailDto(project.getId(), project.getTitleEn(), project.getTitleZh(),
                project.getDescriptionEn(), project.getDescriptionZh(), project.getCategory().name(),
                project.getLocation(), project.getCompletedDate(), project.getCoverImageUrl(),
                project.isPublished(), images, avg, count);
    }

    private double averageRating(Long projectId) {
        Double avg = reviewRepository.findAverageRatingByProjectId(projectId);
        return avg == null ? 0.0 : Math.round(avg * 10) / 10.0;
    }
}
