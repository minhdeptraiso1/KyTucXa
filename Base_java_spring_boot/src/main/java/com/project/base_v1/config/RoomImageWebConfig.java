package com.project.base_v1.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class RoomImageWebConfig implements WebMvcConfigurer {

    private final Path storageDirectory;

    public RoomImageWebConfig(
            @Value("${app.storage.room-images:./uploads/rooms}") String storageDirectory) {
        this.storageDirectory = Path.of(storageDirectory).toAbsolutePath().normalize();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/rooms/**")
                .addResourceLocations(storageDirectory.toUri().toString());
    }
}
