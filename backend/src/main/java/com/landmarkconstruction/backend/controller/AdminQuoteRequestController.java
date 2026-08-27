package com.landmarkconstruction.backend.controller;

import com.landmarkconstruction.backend.dto.common.PageResponse;
import com.landmarkconstruction.backend.dto.quote.QuoteRequestResponse;
import com.landmarkconstruction.backend.dto.quote.QuoteStatusUpdateRequest;
import com.landmarkconstruction.backend.service.QuoteRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/quote-requests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminQuoteRequestController {

    private final QuoteRequestService quoteRequestService;

    @GetMapping
    public PageResponse<QuoteRequestResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return PageResponse.of(quoteRequestService.list(status, pageable));
    }

    @GetMapping("/{id}")
    public QuoteRequestResponse get(@PathVariable Long id) {
        return quoteRequestService.get(id);
    }

    @PatchMapping("/{id}/status")
    public QuoteRequestResponse updateStatus(@PathVariable Long id, @Valid @RequestBody QuoteStatusUpdateRequest request) {
        return quoteRequestService.updateStatus(id, request);
    }
}
