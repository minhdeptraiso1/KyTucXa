package com.project.base_v1.service;

import org.springframework.web.multipart.MultipartFile;

public interface RoomImageStorageService {

    String store(MultipartFile file);
}
