package com.project.base_v1.controller;

import com.project.base_v1.dto.request.assignment.CreateAssignmentRequest;
import com.project.base_v1.dto.response.assignment.AssignmentResponse;
import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.service.RoomAssignmentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Room assignment", description = "Phân và kết thúc giường ở")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/room-assignments")
@RequiredArgsConstructor
public class RoomAssignmentController {
    private final RoomAssignmentService assignmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<AssignmentResponse> assign(@Valid @RequestBody CreateAssignmentRequest request) {
        return ApiResponseSever.ok(assignmentService.assign(request));
    }

    @PatchMapping("/{id}/end")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<AssignmentResponse> end(@PathVariable UUID id) {
        return ApiResponseSever.ok(assignmentService.end(id));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ApiResponseSever<List<AssignmentResponse>> mine() {
        return ApiResponseSever.ok(assignmentService.getMyAssignments());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<Page<AssignmentResponse>> all(@ParameterObject Pageable pageable) {
        return ApiResponseSever.ok(assignmentService.getAll(pageable));
    }
}
