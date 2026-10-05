package com.project.base_v1.dto.response.student;

import lombok.Builder;

import java.util.List;

@Builder
public record StudentImportResultResponse(
        int totalProcessed,
        int insertedCount,
        int updatedCount,
        int skippedCount,
        List<String> messages
) {
}
