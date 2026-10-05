package com.project.base_v1.service;

public interface TokenDeliveryService {
    void deliverEmailVerification(String email, String rawToken);
    void deliverPasswordReset(String email, String rawToken);
}
