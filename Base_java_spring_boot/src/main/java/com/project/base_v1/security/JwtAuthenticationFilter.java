package com.project.base_v1.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.dto.response.core.ErrorResponseSever;
import com.project.base_v1.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    JwtTokenProvider jwtTokenProvider;
    TokenBlacklistService tokenBlacklistService;
    ObjectMapper objectMapper;


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getServletPath();
        return path.startsWith("/auth/")
                || path.startsWith("/swagger-ui/")
                || path.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            // 1️⃣ Check revoked token (Redis)
            if (tokenBlacklistService.isRevoked(token)) {
                writeUnauthorized(response, ErrorCode.TOKEN_REVOKED);
                return;
            }

            // 2️⃣ Validate JWT
            Claims claims;
            try {
                claims = jwtTokenProvider.validateToken(token).getBody();
            } catch (ExpiredJwtException ex) {
                writeUnauthorized(response, ErrorCode.TOKEN_EXPIRED);
                return;
            } catch (JwtException | IllegalArgumentException ex) {
                writeUnauthorized(response, ErrorCode.INVALID_TOKEN);
                return;
            }
            String role = claims.get("role", String.class);
            List<?> permissions = claims.get("permissions", List.class);
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
            if (permissions != null) {
                permissions.stream()
                        .map(String::valueOf)
                        .map(SimpleGrantedAuthority::new)
                        .forEach(authorities::add);
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            claims.get("username"),
                            null,
                            authorities
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private void writeUnauthorized(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), ApiResponseSever.error(
                new ErrorResponseSever(errorCode.code(), errorCode.message())));
    }
}
