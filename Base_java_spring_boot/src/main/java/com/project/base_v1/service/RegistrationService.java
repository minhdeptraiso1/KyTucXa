package com.project.base_v1.service;

import com.project.base_v1.dto.request.registration.*;
import com.project.base_v1.dto.response.registration.RegistrationResponse;
import com.project.base_v1.enums.RegistrationStatus;
import org.springframework.data.domain.*;
import java.util.*;

public interface RegistrationService {
    RegistrationResponse createMyRegistration(CreateRegistrationRequest request);
    List<RegistrationResponse> getMyRegistrations();
    RegistrationResponse cancelMyRegistration(UUID id);
    Page<RegistrationResponse> getQueue(RegistrationStatus status, String keyword, Pageable pageable);
    RegistrationResponse review(UUID id, ReviewRegistrationRequest request);
}
