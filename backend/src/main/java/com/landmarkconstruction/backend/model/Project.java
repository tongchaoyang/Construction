package com.landmarkconstruction.backend.model;

import com.landmarkconstruction.backend.model.enums.ProjectCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title_en", nullable = false)
    private String titleEn;

    @Column(name = "title_zh", nullable = false)
    private String titleZh;

    @Column(name = "description_en", nullable = false, columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(name = "description_zh", nullable = false, columnDefinition = "TEXT")
    private String descriptionZh;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProjectCategory category;

    private String location;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    @Column(name = "cover_image_url", length = 1000)
    private String coverImageUrl;

    @Column(nullable = false)
    private boolean published = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectImage> images = new ArrayList<>();

    public List<ProjectImage> getImagesOrdered() {
        return images.stream()
                .sorted(Comparator.comparingInt(ProjectImage::getSortOrder))
                .toList();
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
