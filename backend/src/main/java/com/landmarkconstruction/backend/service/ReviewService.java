package com.landmarkconstruction.backend.service;

import com.landmarkconstruction.backend.dto.review.ReviewRequest;
import com.landmarkconstruction.backend.dto.review.ReviewResponse;
import com.landmarkconstruction.backend.exception.DuplicateReviewException;
import com.landmarkconstruction.backend.exception.ResourceNotFoundException;
import com.landmarkconstruction.backend.model.Project;
import com.landmarkconstruction.backend.model.Review;
import com.landmarkconstruction.backend.model.User;
import com.landmarkconstruction.backend.repository.ProjectRepository;
import com.landmarkconstruction.backend.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProjectRepository projectRepository;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public Page<ReviewResponse> list(Long projectId, Pageable pageable) {
        return reviewRepository.findByProjectId(projectId, pageable).map(this::toResponse);
    }

    @Transactional
    public ReviewResponse create(Long projectId, User user, ReviewRequest request) {
        if (reviewRepository.existsByProjectIdAndUserId(projectId, user.getId())) {
            throw new DuplicateReviewException("You have already reviewed this project. Edit your existing review instead.");
        }
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
        Review review = new Review();
        review.setProject(project);
        review.setUser(user);
        review.setRating(request.rating());
        review.setComment(request.comment());
        reviewRepository.save(review);
        emailService.sendReviewConfirmationToCustomer(user, project, review);
        return toResponse(review);
    }

    @Transactional
    public ReviewResponse update(Long projectId, User user, ReviewRequest request) {
        Review review = reviewRepository.findByProjectIdAndUserId(projectId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("You have not reviewed this project yet."));
        review.setRating(request.rating());
        review.setComment(request.comment());
        return toResponse(review);
    }

    @Transactional
    public void deleteOwn(Long projectId, User user) {
        Review review = reviewRepository.findByProjectIdAndUserId(projectId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("You have not reviewed this project yet."));
        reviewRepository.delete(review);
    }

    @Transactional
    public void deleteAsAdmin(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + reviewId));
        reviewRepository.delete(review);
    }

    private ReviewResponse toResponse(Review review) {
        return new ReviewResponse(review.getId(), review.getProject().getId(), review.getUser().getId(),
                review.getUser().getFullName(), review.getRating(), review.getComment(),
                review.getCreatedAt(), review.getUpdatedAt());
    }
}
