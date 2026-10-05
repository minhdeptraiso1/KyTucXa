package com.project.base_v1.service;

import com.project.base_v1.dto.request.assignment.CreateAssignmentRequest;
import com.project.base_v1.dto.response.assignment.AssignmentResponse;
import org.springframework.data.domain.*;
import java.util.*;

public interface RoomAssignmentService {
    AssignmentResponse assign(CreateAssignmentRequest request);
    AssignmentResponse end(UUID id);
    List<AssignmentResponse> getMyAssignments();
    Page<AssignmentResponse> getAll(Pageable pageable);
}
