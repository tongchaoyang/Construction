package com.landmarkconstruction.backend.controller;

import com.landmarkconstruction.backend.dto.common.PageResponse;
import com.landmarkconstruction.backend.dto.review.ReviewRequest;
import com.landmarkconstruction.backend.dto.review.ReviewResponse;
import com.landmarkconstruction.backend.security.CurrentUser;
import com.landmarkconstruction.backend.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects/{projectId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final CurrentUser currentUser;

    @GetMapping
    public PageResponse<ReviewResponse> list(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return PageResponse.of(reviewService.list(projectId, pageable));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse create(@PathVariable Long projectId, @Valid @RequestBody ReviewRequest request) {
        return reviewService.create(projectId, currentUser.require(), request);
    }

    @PutMapping("/me")
    public ReviewResponse update(@PathVariable Long projectId, @Valid @RequestBody ReviewRequest request) {
        return reviewService.update(projectId, currentUser.require(), request);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOwn(@PathVariable Long projectId) {
        reviewService.deleteOwn(projectId, currentUser.require());
    }
}
