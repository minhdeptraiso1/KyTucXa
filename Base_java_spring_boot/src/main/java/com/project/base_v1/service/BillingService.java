package com.project.base_v1.service;

import com.project.base_v1.dto.request.billing.*;
import com.project.base_v1.dto.response.billing.*;
import com.project.base_v1.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface BillingService {
    MeterResponse createMeter(CreateMeterRequest request);
    MeterResponse updateMeter(UUID id, UpdateMeterRequest request);
    List<MeterResponse> getMeters();
    MeterReadingResponse recordReading(CreateMeterReadingRequest request);
    MeterReadingResponse updateReading(UUID id, UpdateMeterReadingRequest request);
    List<MeterReadingResponse> getReadings();
    TariffResponse createTariff(CreateTariffRequest request);
    TariffResponse updateTariff(UUID id, UpdateTariffRequest request);
    List<TariffResponse> getTariffs();
    InvoiceResponse generateInvoice(GenerateInvoiceRequest request);
    Page<InvoiceResponse> getInvoices(InvoiceStatus status, Pageable pageable);
    List<InvoiceResponse> getMyInvoices();
    InvoiceResponse getInvoice(UUID id);
    PaymentResponse recordCashPayment(CreatePaymentRequest request);
    VnPayUrlResponse createVnPayPayment(CreatePaymentRequest request, String ipAddress);
    PaymentResponse handleVnPayCallback(Map<String, String> parameters);
    List<PaymentResponse> getPayments();
    List<PaymentResponse> getMyPayments();
}

