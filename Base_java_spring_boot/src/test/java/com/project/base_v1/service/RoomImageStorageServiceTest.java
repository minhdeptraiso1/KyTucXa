package com.project.base_v1.service;

import com.project.base_v1.exception.BusinessException;
import com.project.base_v1.exception.ErrorCode;
import com.project.base_v1.service.impl.RoomImageStorageServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomImageStorageServiceTest {

    @TempDir
    Path tempDirectory;

    @Test
    void storesSupportedImageWithGeneratedName() throws Exception {
        var service = new RoomImageStorageServiceImpl(tempDirectory.toString());
        var image = new MockMultipartFile("file", "room.png", "image/png", new byte[]{1, 2, 3});

        String fileName = service.store(image);

        assertTrue(fileName.endsWith(".png"));
        assertTrue(Files.exists(tempDirectory.resolve(fileName)));
    }

    @Test
    void rejectsUnsupportedFileType() {
        var service = new RoomImageStorageServiceImpl(tempDirectory.toString());
        var file = new MockMultipartFile("file", "room.svg", "image/svg+xml", "<svg/>".getBytes());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.store(file));

        assertEquals(ErrorCode.INVALID_IMAGE_FILE, exception.getErrorCode());
    }
}
