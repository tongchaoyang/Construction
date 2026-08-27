package com.landmarkconstruction.backend.controller;

import com.landmarkconstruction.backend.dto.quote.QuoteRequestResponse;
import com.landmarkconstruction.backend.security.CurrentUser;
import com.landmarkconstruction.backend.service.QuoteRequestService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/quote-requests")
@RequiredArgsConstructor
public class QuoteRequestController {

    private final QuoteRequestService quoteRequestService;
    private final CurrentUser currentUser;

    @PostMapping(consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public QuoteRequestResponse create(
            @RequestParam @NotBlank String name,
            @RequestParam @NotBlank String email,
            @RequestParam @NotBlank String phone,
            @RequestParam @NotBlank String category,
            @RequestParam @NotBlank String description,
            @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        var linkedUser = currentUser.get().orElse(null);
        return quoteRequestService.create(name, email, phone, category, description,
                files == null ? Collections.emptyList() : files, linkedUser);
    }
}
