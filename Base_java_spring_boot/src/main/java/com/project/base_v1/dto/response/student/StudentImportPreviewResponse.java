package com.project.base_v1.dto.response.student;

import lombok.Builder;

import java.util.List;

@Builder
public record StudentImportPreviewResponse(
        int totalRows,
        int validRows,
        int invalidRows,
        int duplicateRows,
        int newCount,
        int updateCount,
        List<String> errors,
        List<StudentRegistryRowDto> previewRows
) {
}
