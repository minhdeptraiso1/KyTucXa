package com.project.base_v1.service;

import com.project.base_v1.dto.request.facility.CreatePricePolicyRequest;
import com.project.base_v1.dto.request.facility.UpdatePricePolicyRequest;
import com.project.base_v1.dto.response.facility.PricePolicyResponse;

import java.util.List;
import java.util.UUID;

public interface PricePolicyService {

    PricePolicyResponse createPolicy(CreatePricePolicyRequest request);

    PricePolicyResponse updatePolicy(UUID id, UpdatePricePolicyRequest request);

    PricePolicyResponse getPolicyById(UUID id);

    List<PricePolicyResponse> getAllPolicies();

    List<PricePolicyResponse> getActivePolicies();

    void deletePolicy(UUID id);
}
