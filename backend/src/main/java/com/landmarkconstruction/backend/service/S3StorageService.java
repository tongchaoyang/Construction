package com.landmarkconstruction.backend.service;

import com.landmarkconstruction.backend.exception.BadRequestException;
import com.landmarkconstruction.backend.exception.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class S3StorageService implements StorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_FILE_SIZE_BYTES = 8L * 1024 * 1024;

    private final S3Client s3Client;
    private final String bucket;
    private final String publicBaseUrl;

    public S3StorageService(S3Client s3Client,
                             @Value("${s3.bucket}") String bucket,
                             @Value("${s3.public-base-url}") String publicBaseUrl) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.publicBaseUrl = publicBaseUrl;
    }

    @Override
    public String upload(MultipartFile file, String folder) {
        validate(file);
        String contentType = file.getContentType();
        String extension = extensionFor(contentType);
        String key = folder + "/" + UUID.randomUUID() + extension;

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new FileStorageException("Failed to read uploaded file", e);
        } catch (Exception e) {
            log.error("Failed to upload file to S3", e);
            throw new FileStorageException("Failed to upload file to storage", e);
        }

        return publicBaseUrl + "/" + key;
    }

    @Override
    public void delete(String imageUrl) {
        if (!StringUtils.hasText(imageUrl) || !imageUrl.startsWith(publicBaseUrl)) {
            return;
        }
        String key = imageUrl.substring(publicBaseUrl.length() + 1);
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
        } catch (Exception e) {
            log.warn("Failed to delete object {} from S3", key, e);
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file is empty.");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BadRequestException("File exceeds the 8MB size limit: " + file.getOriginalFilename());
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Unsupported file type. Only JPEG, PNG, and WEBP images are allowed.");
        }
    }

    private String extensionFor(String contentType) {
        return switch (contentType.toLowerCase()) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }

    public static void validateBatch(List<MultipartFile> files, int maxCount) {
        if (files != null && files.size() > maxCount) {
            throw new BadRequestException("You may upload at most " + maxCount + " photos.");
        }
    }
}
