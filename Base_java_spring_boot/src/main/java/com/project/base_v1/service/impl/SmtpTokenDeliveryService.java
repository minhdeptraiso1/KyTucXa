package com.project.base_v1.service.impl;

import com.project.base_v1.service.TokenDeliveryService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true")
public class SmtpTokenDeliveryService implements TokenDeliveryService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void deliverEmailVerification(String email, String rawToken) {
        String url = frontendUrl + "/verify-email?token=" + rawToken;
        send(email, "Kích hoạt tài khoản KTX UET", """
                <h2>Kích hoạt tài khoản KTX UET</h2>
                <p>Bạn vừa đăng ký tài khoản quản lý nội trú.</p>
                <p><a href="%s">Xác minh email và kích hoạt tài khoản</a></p>
                <p>Liên kết có hiệu lực trong 24 giờ. Nếu bạn không thực hiện đăng ký, hãy bỏ qua email này.</p>
                """.formatted(url));
    }

    @Override
    public void deliverPasswordReset(String email, String rawToken) {
        String url = frontendUrl + "/reset-password?token=" + rawToken;
        send(email, "Đặt lại mật khẩu KTX UET", """
                <h2>Đặt lại mật khẩu</h2>
                <p><a href="%s">Tạo mật khẩu mới</a></p>
                <p>Liên kết có hiệu lực trong 30 phút. Nếu bạn không yêu cầu, hãy bỏ qua email này.</p>
                """.formatted(url));
    }

    private void send(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception exception) {
            throw new IllegalStateException("Không thể gửi email qua SMTP", exception);
        }
    }
}
