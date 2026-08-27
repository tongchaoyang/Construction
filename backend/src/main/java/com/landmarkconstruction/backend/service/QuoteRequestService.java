package com.landmarkconstruction.backend.service;

import com.landmarkconstruction.backend.dto.quote.QuoteRequestResponse;
import com.landmarkconstruction.backend.dto.quote.QuoteStatusUpdateRequest;
import com.landmarkconstruction.backend.exception.BadRequestException;
import com.landmarkconstruction.backend.exception.ResourceNotFoundException;
import com.landmarkconstruction.backend.model.QuoteRequest;
import com.landmarkconstruction.backend.model.QuoteRequestImage;
import com.landmarkconstruction.backend.model.User;
import com.landmarkconstruction.backend.model.enums.QuoteCategory;
import com.landmarkconstruction.backend.model.enums.QuoteStatus;
import com.landmarkconstruction.backend.repository.QuoteRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuoteRequestService {

    private final QuoteRequestRepository quoteRequestRepository;
    private final StorageService storageService;
    private final EmailService emailService;

    @Transactional
    public QuoteRequestResponse create(String name, String email, String phone, String categoryStr,
                                        String description, List<MultipartFile> files, User linkedUser) {
        S3StorageService.validateBatch(files, 10);

        QuoteRequest quoteRequest = new QuoteRequest();
        quoteRequest.setName(name);
        quoteRequest.setEmail(email);
        quoteRequest.setPhone(phone);
        quoteRequest.setCategory(parseCategory(categoryStr));
        quoteRequest.setDescription(description);
        quoteRequest.setStatus(QuoteStatus.NEW);
        quoteRequest.setUser(linkedUser);
        quoteRequestRepository.save(quoteRequest);

        if (files != null) {
            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue;
                }
                String url = storageService.upload(file, "quotes/" + quoteRequest.getId());
                QuoteRequestImage image = new QuoteRequestImage();
                image.setQuoteRequest(quoteRequest);
                image.setImageUrl(url);
                quoteRequest.getImages().add(image);
            }
            quoteRequestRepository.save(quoteRequest);
        }

        emailService.sendQuoteNotificationToBusiness(quoteRequest);
        emailService.sendQuoteConfirmationToCustomer(quoteRequest);

        return toResponse(quoteRequest);
    }

    @Transactional(readOnly = true)
    public Page<QuoteRequestResponse> list(String statusStr, Pageable pageable) {
        if (statusStr != null && !statusStr.isBlank()) {
            QuoteStatus status = parseStatus(statusStr);
            return quoteRequestRepository.findByStatus(status, pageable).map(this::toResponse);
        }
        return quoteRequestRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public QuoteRequestResponse get(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public QuoteRequestResponse updateStatus(Long id, QuoteStatusUpdateRequest request) {
        QuoteRequest quoteRequest = findOrThrow(id);
        quoteRequest.setStatus(parseStatus(request.status()));
        if (request.adminNotes() != null) {
            quoteRequest.setAdminNotes(request.adminNotes());
        }
        return toResponse(quoteRequest);
    }

    private QuoteRequest findOrThrow(Long id) {
        return quoteRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quote request not found: " + id));
    }

    private QuoteCategory parseCategory(String value) {
        try {
            return QuoteCategory.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid category: " + value);
        }
    }

    private QuoteStatus parseStatus(String value) {
        try {
            return QuoteStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + value);
        }
    }

    private QuoteRequestResponse toResponse(QuoteRequest quoteRequest) {
        List<String> imageUrls = quoteRequest.getImages().stream().map(QuoteRequestImage::getImageUrl).toList();
        return new QuoteRequestResponse(quoteRequest.getId(), quoteRequest.getName(), quoteRequest.getEmail(),
                quoteRequest.getPhone(), quoteRequest.getCategory().name(), quoteRequest.getDescription(),
                quoteRequest.getStatus().name(), quoteRequest.getAdminNotes(), quoteRequest.getCreatedAt(), imageUrls);
    }
}
