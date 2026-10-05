package com.project.base_v1.security;

public final class SecurityEndpoints {

    private SecurityEndpoints() {}

    public static final String[] PUBLIC = {
            "/auth/**",
            "/public/**",
            "/payments/vnpay/callback",
            "/payments/vnpay/ipn",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };
}
