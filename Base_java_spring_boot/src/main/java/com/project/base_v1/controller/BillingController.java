package com.project.base_v1.controller;

import com.project.base_v1.dto.request.billing.*;
import com.project.base_v1.dto.response.billing.*;
import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.enums.InvoiceStatus;
import com.project.base_v1.service.BillingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class BillingController {
    private final BillingService billingService;

    @PostMapping("/meters")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<MeterResponse> createMeter(@Valid @RequestBody CreateMeterRequest request) {
        return ApiResponseSever.ok(billingService.createMeter(request));
    }

    @PutMapping("/meters/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<MeterResponse> updateMeter(
            @PathVariable UUID id, @Valid @RequestBody UpdateMeterRequest request) {
        return ApiResponseSever.ok(billingService.updateMeter(id, request));
    }

    @GetMapping("/meters")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<List<MeterResponse>> meters() {
        return ApiResponseSever.ok(billingService.getMeters());
    }

    @PostMapping("/meter-readings")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<MeterReadingResponse> createReading(@Valid @RequestBody CreateMeterReadingRequest request) {
        return ApiResponseSever.ok(billingService.recordReading(request));
    }

    @PutMapping("/meter-readings/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<MeterReadingResponse> updateReading(
            @PathVariable UUID id, @Valid @RequestBody UpdateMeterReadingRequest request) {
        return ApiResponseSever.ok(billingService.updateReading(id, request));
    }

    @GetMapping("/meter-readings")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<List<MeterReadingResponse>> readings() {
        return ApiResponseSever.ok(billingService.getReadings());
    }

    @PostMapping("/utility-tariffs")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<TariffResponse> createTariff(@Valid @RequestBody CreateTariffRequest request) {
        return ApiResponseSever.ok(billingService.createTariff(request));
    }

    @PutMapping("/utility-tariffs/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<TariffResponse> updateTariff(
            @PathVariable UUID id, @Valid @RequestBody UpdateTariffRequest request) {
        return ApiResponseSever.ok(billingService.updateTariff(id, request));
    }

    @GetMapping("/utility-tariffs")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<List<TariffResponse>> tariffs() {
        return ApiResponseSever.ok(billingService.getTariffs());
    }

    @PostMapping("/invoices/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<InvoiceResponse> generateInvoice(@Valid @RequestBody GenerateInvoiceRequest request) {
        return ApiResponseSever.ok(billingService.generateInvoice(request));
    }

    @GetMapping("/invoices")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<Page<InvoiceResponse>> invoices(
            @RequestParam(required = false) InvoiceStatus status, @ParameterObject Pageable pageable) {
        return ApiResponseSever.ok(billingService.getInvoices(status, pageable));
    }

    @GetMapping("/invoices/my")
    @PreAuthorize("hasRole('USER')")
    public ApiResponseSever<List<InvoiceResponse>> myInvoices() {
        return ApiResponseSever.ok(billingService.getMyInvoices());
    }

    @GetMapping("/invoices/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public ApiResponseSever<InvoiceResponse> invoice(@PathVariable UUID id) {
        return ApiResponseSever.ok(billingService.getInvoice(id));
    }

    @PostMapping("/payments/direct")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<PaymentResponse> directPayment(@Valid @RequestBody CreatePaymentRequest request) {
        return ApiResponseSever.ok(billingService.recordCashPayment(request));
    }

    @PostMapping("/payments/vnpay/create")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'USER')")
    public ApiResponseSever<VnPayUrlResponse> createVnPay(
            @Valid @RequestBody CreatePaymentRequest request, HttpServletRequest servletRequest) {
        return ApiResponseSever.ok(billingService.createVnPayPayment(request, clientIp(servletRequest)));
    }

    @GetMapping({"/payments/vnpay/callback", "/payments/vnpay/ipn"})
    public ApiResponseSever<PaymentResponse> vnPayCallback(@RequestParam Map<String, String> parameters) {
        return ApiResponseSever.ok(billingService.handleVnPayCallback(parameters));
    }

    @GetMapping("/payments")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponseSever<List<PaymentResponse>> payments() {
        return ApiResponseSever.ok(billingService.getPayments());
    }

    @GetMapping("/payments/my")
    @PreAuthorize("hasRole('USER')")
    public ApiResponseSever<List<PaymentResponse>> myPayments() {
        return ApiResponseSever.ok(billingService.getMyPayments());
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded == null || forwarded.isBlank() ? request.getRemoteAddr() : forwarded.split(",")[0].trim();
    }
}

