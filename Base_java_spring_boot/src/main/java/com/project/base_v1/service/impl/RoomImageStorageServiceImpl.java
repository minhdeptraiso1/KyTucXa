package com.project.base_v1.service.impl;

import com.project.base_v1.exception.BusinessException;
import com.project.base_v1.exception.ErrorCode;
import com.project.base_v1.service.RoomImageStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Service
public class RoomImageStorageServiceImpl implements RoomImageStorageService {

    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;
    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp"
    );

    private final Path storageDirectory;

    public RoomImageStorageServiceImpl(
            @Value("${app.storage.room-images:./uploads/rooms}") String storageDirectory) {
        this.storageDirectory = Path.of(storageDirectory).toAbsolutePath().normalize();
    }

    @Override
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() > MAX_IMAGE_BYTES) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_FILE);
        }

        String extension = EXTENSIONS.get(file.getContentType());
        if (extension == null) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_FILE);
        }

        String fileName = UUID.randomUUID() + extension;
        Path target = storageDirectory.resolve(fileName).normalize();
        if (!target.startsWith(storageDirectory)) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_FILE);
        }

        try {
            Files.createDirectories(storageDirectory);
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return fileName;
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.IMAGE_STORAGE_ERROR);
        }
    }
}
