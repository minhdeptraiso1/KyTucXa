package com.project.base_v1.service.impl;

import com.project.base_v1.dto.request.facility.CreatePricePolicyRequest;
import com.project.base_v1.dto.request.facility.UpdatePricePolicyRequest;
import com.project.base_v1.dto.response.facility.PricePolicyResponse;
import com.project.base_v1.entity.PricePolicy;
import com.project.base_v1.exception.BusinessException;
import com.project.base_v1.exception.ErrorCode;
import com.project.base_v1.repository.PricePolicyRepository;
import com.project.base_v1.security.CurrentUser;
import com.project.base_v1.service.PricePolicyService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PricePolicyServiceImpl implements PricePolicyService {

    PricePolicyRepository pricePolicyRepository;

    @Override
    @Transactional
    public PricePolicyResponse createPolicy(CreatePricePolicyRequest request) {
        if (request.effectiveTo() != null && !request.effectiveTo().isAfter(request.effectiveFrom())) {
            throw new BusinessException(ErrorCode.PRICE_POLICY_INVALID_DATES);
        }

        PricePolicy policy = PricePolicy.builder()
                .id(UUID.randomUUID())
                .name(request.name().trim())
                .roomType(request.roomType())
                .pricePerMonth(request.pricePerMonth())
                .effectiveFrom(request.effectiveFrom())
                .effectiveTo(request.effectiveTo())
                .isActive(request.isActive() != null ? request.isActive() : true)
                .description(request.description())
                .build();

        PricePolicy saved = pricePolicyRepository.save(policy);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public PricePolicyResponse updatePolicy(UUID id, UpdatePricePolicyRequest request) {
        PricePolicy policy = pricePolicyRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRICE_POLICY_NOT_FOUND));

        if (request.effectiveTo() != null && !request.effectiveTo().isAfter(policy.getEffectiveFrom())) {
            throw new BusinessException(ErrorCode.PRICE_POLICY_INVALID_DATES);
        }

        policy.setName(request.name().trim());
        policy.setPricePerMonth(request.pricePerMonth());
        policy.setEffectiveTo(request.effectiveTo());
        policy.setActive(request.isActive());
        policy.setDescription(request.description());

        PricePolicy updated = pricePolicyRepository.save(policy);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public PricePolicyResponse getPolicyById(UUID id) {
        PricePolicy policy = pricePolicyRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRICE_POLICY_NOT_FOUND));
        return mapToResponse(policy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PricePolicyResponse> getAllPolicies() {
        return pricePolicyRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PricePolicyResponse> getActivePolicies() {
        return pricePolicyRepository.findByIsActiveTrue().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deletePolicy(UUID id) {
        PricePolicy policy = pricePolicyRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRICE_POLICY_NOT_FOUND));

        policy.setDeletedAt(Instant.now());
        policy.setDeletedBy(CurrentUser.username());
        pricePolicyRepository.save(policy);
    }

    private PricePolicyResponse mapToResponse(PricePolicy p) {
        return PricePolicyResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .roomType(p.getRoomType())
                .pricePerMonth(p.getPricePerMonth())
                .effectiveFrom(p.getEffectiveFrom())
                .effectiveTo(p.getEffectiveTo())
                .isActive(p.isActive())
                .description(p.getDescription())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
