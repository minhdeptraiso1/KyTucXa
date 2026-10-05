package com.project.base_v1.service.impl;

import com.project.base_v1.service.TokenDeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "false", matchIfMissing = true)
public class DevelopmentTokenDeliveryService implements TokenDeliveryService {
    @Override
    public void deliverEmailVerification(String email, String rawToken) {
        log.info("Development email verification token for {}: {}", email, rawToken);
    }

    @Override
    public void deliverPasswordReset(String email, String rawToken) {
        log.info("Development password reset token for {}: {}", email, rawToken);
    }
}
