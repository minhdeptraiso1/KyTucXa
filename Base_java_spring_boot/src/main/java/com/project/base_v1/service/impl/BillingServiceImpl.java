package com.project.base_v1.service.impl;

import com.project.base_v1.config.VnPayProperties;
import com.project.base_v1.dto.request.billing.*;
import com.project.base_v1.dto.response.billing.*;
import com.project.base_v1.entity.*;
import com.project.base_v1.enums.*;
import com.project.base_v1.exception.BusinessException;
import com.project.base_v1.exception.ErrorCode;
import com.project.base_v1.repository.*;
import com.project.base_v1.security.CurrentUser;
import com.project.base_v1.service.AuditLogService;
import com.project.base_v1.service.BillingService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {
    private final UtilityMeterRepository meterRepository;
    private final MeterReadingRepository readingRepository;
    private final UtilityTariffRepository tariffRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final RoomRepository roomRepository;
    private final ContractRepository contractRepository;
    private final RoomAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final AuditLogService auditLogService;
    private final VnPayProperties vnPayProperties;

    @Override
    @Transactional
    public MeterResponse createMeter(CreateMeterRequest request) {
        Room room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
        if (meterRepository.existsByMeterCodeIgnoreCase(request.meterCode().trim())
                || meterRepository.existsByRoomIdAndUtilityTypeAndStatus(room.getId(), request.utilityType(), MeterStatus.ACTIVE)) {
            throw new BusinessException(ErrorCode.METER_ALREADY_EXISTS);
        }
        UtilityMeter meter = meterRepository.save(UtilityMeter.builder()
                .id(UUID.randomUUID()).room(room).meterCode(request.meterCode().trim())
                .utilityType(request.utilityType()).unit(request.unit().trim()).status(MeterStatus.ACTIVE).build());
        auditLogService.log(currentUser().getId(), AuditAction.CREATE_METER.name());
        return toMeter(meter);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeterResponse> getMeters() {
        return meterRepository.findAllByOrderByMeterCodeAsc().stream().map(this::toMeter).toList();
    }

    @Override
    @Transactional
    public MeterResponse updateMeter(UUID id, UpdateMeterRequest request) {
        UtilityMeter meter = meterRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.METER_NOT_FOUND));
        String meterCode = request.meterCode().trim();
        if (!meter.getMeterCode().equalsIgnoreCase(meterCode)
                && meterRepository.existsByMeterCodeIgnoreCase(meterCode)) {
            throw new BusinessException(ErrorCode.METER_ALREADY_EXISTS);
        }
        meter.setMeterCode(meterCode);
        meter.setUnit(request.unit().trim());
        meter.setStatus(request.status());
        return toMeter(meterRepository.save(meter));
    }

    @Override
    @Transactional
    public MeterReadingResponse recordReading(CreateMeterReadingRequest request) {
        validatePeriod(request.billingPeriod());
        UtilityMeter meter = meterRepository.findById(request.meterId())
                .orElseThrow(() -> new BusinessException(ErrorCode.METER_NOT_FOUND));
        if (readingRepository.existsByMeterIdAndBillingPeriod(meter.getId(), request.billingPeriod())) {
            throw new BusinessException(ErrorCode.METER_READING_ALREADY_EXISTS);
        }
        Optional<MeterReading> latest = readingRepository.findFirstByMeterIdOrderByBillingPeriodDesc(meter.getId());
        if (latest.isPresent() && !request.billingPeriod().isAfter(latest.get().getBillingPeriod())) {
            throw new BusinessException(ErrorCode.INVALID_METER_READING);
        }
        BigDecimal previous = request.resetRecorded() ? BigDecimal.ZERO
                : latest.map(MeterReading::getCurrentValue).orElse(BigDecimal.ZERO);
        if (!request.resetRecorded() && request.currentValue().compareTo(previous) < 0) {
            throw new BusinessException(ErrorCode.INVALID_METER_READING);
        }
        BigDecimal consumption = request.resetRecorded() ? request.currentValue()
                : request.currentValue().subtract(previous);
        MeterReading reading = readingRepository.save(MeterReading.builder()
                .id(UUID.randomUUID()).meter(meter).billingPeriod(request.billingPeriod())
                .previousValue(previous.setScale(3, RoundingMode.HALF_UP))
                .currentValue(request.currentValue().setScale(3, RoundingMode.HALF_UP))
                .consumption(consumption.setScale(3, RoundingMode.HALF_UP))
                .resetRecorded(request.resetRecorded()).note(blankToNull(request.note())).readAt(Instant.now()).build());
        auditLogService.log(currentUser().getId(), AuditAction.RECORD_METER_READING.name());
        return toReading(reading);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeterReadingResponse> getReadings() {
        return readingRepository.findAllByOrderByBillingPeriodDesc().stream().map(this::toReading).toList();
    }

    @Override
    @Transactional
    public MeterReadingResponse updateReading(UUID id, UpdateMeterReadingRequest request) {
        MeterReading reading = readingRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        Optional<MeterReading> latest = readingRepository.findFirstByMeterIdOrderByBillingPeriodDesc(reading.getMeter().getId());
        boolean invoiced = invoiceRepository.existsIssuedForRoomAndPeriod(
                reading.getMeter().getRoom().getId(), reading.getBillingPeriod());
        if (latest.isEmpty() || !latest.get().getId().equals(reading.getId()) || invoiced) {
            throw new BusinessException(ErrorCode.METER_READING_LOCKED);
        }
        BigDecimal previous = request.resetRecorded() ? BigDecimal.ZERO
                : readingRepository.findFirstByMeterIdAndBillingPeriodBeforeOrderByBillingPeriodDesc(
                        reading.getMeter().getId(), reading.getBillingPeriod())
                    .map(MeterReading::getCurrentValue).orElse(BigDecimal.ZERO);
        if (!request.resetRecorded() && request.currentValue().compareTo(previous) < 0) {
            throw new BusinessException(ErrorCode.INVALID_METER_READING);
        }
        reading.setPreviousValue(previous.setScale(3, RoundingMode.HALF_UP));
        reading.setCurrentValue(request.currentValue().setScale(3, RoundingMode.HALF_UP));
        reading.setConsumption((request.resetRecorded() ? request.currentValue()
                : request.currentValue().subtract(previous)).setScale(3, RoundingMode.HALF_UP));
        reading.setResetRecorded(request.resetRecorded());
        reading.setNote(blankToNull(request.note()));
        reading.setReadAt(Instant.now());
        return toReading(readingRepository.save(reading));
    }

    @Override
    @Transactional
    public TariffResponse createTariff(CreateTariffRequest request) {
        if (request.effectiveTo() != null && request.effectiveTo().isBefore(request.effectiveFrom())) {
            throw new BusinessException(ErrorCode.PRICE_POLICY_INVALID_DATES);
        }
        UtilityTariff tariff = tariffRepository.save(UtilityTariff.builder()
                .id(UUID.randomUUID()).utilityType(request.utilityType()).unitPrice(money(request.unitPrice()))
                .effectiveFrom(request.effectiveFrom()).effectiveTo(request.effectiveTo()).active(true).build());
        auditLogService.log(currentUser().getId(), AuditAction.CREATE_TARIFF.name());
        return toTariff(tariff);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TariffResponse> getTariffs() {
        return tariffRepository.findAllByOrderByEffectiveFromDesc().stream().map(this::toTariff).toList();
    }

    @Override
    @Transactional
    public TariffResponse updateTariff(UUID id, UpdateTariffRequest request) {
        UtilityTariff tariff = tariffRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TARIFF_NOT_FOUND));
        if (request.effectiveTo() != null && request.effectiveTo().isBefore(request.effectiveFrom())) {
            throw new BusinessException(ErrorCode.PRICE_POLICY_INVALID_DATES);
        }
        tariff.setUnitPrice(money(request.unitPrice()));
        tariff.setEffectiveFrom(request.effectiveFrom());
        tariff.setEffectiveTo(request.effectiveTo());
        tariff.setActive(request.active());
        return toTariff(tariffRepository.save(tariff));
    }

    @Override
    @Transactional
    public InvoiceResponse generateInvoice(GenerateInvoiceRequest request) {
        validatePeriod(request.billingPeriod());
        if (request.dueDate().isBefore(request.billingPeriod())) throw new BusinessException(ErrorCode.INVALID_BILLING_PERIOD);
        Contract contract = contractRepository.findById(request.contractId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CONTRACT_NOT_FOUND));
        if (invoiceRepository.existsByContractIdAndBillingPeriod(contract.getId(), request.billingPeriod())) {
            throw new BusinessException(ErrorCode.INVOICE_ALREADY_EXISTS);
        }
        LocalDate periodStart = request.billingPeriod();
        LocalDate periodEnd = periodStart.withDayOfMonth(periodStart.lengthOfMonth());
        LocalDate stayStart = contract.getStartDate().isAfter(periodStart) ? contract.getStartDate() : periodStart;
        LocalDate stayEnd = contract.getEndDate().isBefore(periodEnd) ? contract.getEndDate() : periodEnd;
        if (stayEnd.isBefore(stayStart)) throw new BusinessException(ErrorCode.INVALID_BILLING_PERIOD);

        Invoice invoice = Invoice.builder().id(UUID.randomUUID()).invoiceCode(nextInvoiceCode())
                .user(contract.getUser()).contract(contract).billingPeriod(periodStart).dueDate(request.dueDate())
                .discount(money(orZero(request.discount()))).fineAmount(money(orZero(request.fineAmount())))
                .paidAmount(BigDecimal.ZERO).status(InvoiceStatus.ISSUED).issuedAt(Instant.now()).build();

        long stayDays = ChronoUnit.DAYS.between(stayStart, stayEnd) + 1;
        BigDecimal roomQuantity = BigDecimal.valueOf(stayDays)
                .divide(BigDecimal.valueOf(periodStart.lengthOfMonth()), 3, RoundingMode.HALF_UP);
        addItem(invoice, InvoiceItemType.ROOM, "Tiền phòng " + contract.getAssignment().getBed().getRoom().getRoomNumber(),
                roomQuantity, contract.getRentalPrice());

        Room room = contract.getAssignment().getBed().getRoom();
        addUtilityItem(invoice, room, contract.getUser(), UtilityType.ELECTRICITY, periodStart, periodEnd);
        addUtilityItem(invoice, room, contract.getUser(), UtilityType.WATER, periodStart, periodEnd);
        if (orZero(request.serviceFee()).signum() > 0) {
            addItem(invoice, InvoiceItemType.SERVICE, "Phí dịch vụ", BigDecimal.ONE, request.serviceFee());
        }
        if (invoice.getFineAmount().signum() > 0) {
            addItem(invoice, InvoiceItemType.FINE, "Khoản phạt phát sinh", BigDecimal.ONE, invoice.getFineAmount());
        }

        BigDecimal subtotal = invoice.getItems().stream()
                .filter(item -> item.getItemType() != InvoiceItemType.FINE)
                .map(InvoiceItem::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (invoice.getDiscount().compareTo(subtotal.add(invoice.getFineAmount())) > 0) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_INVALID);
        }
        invoice.setSubtotal(money(subtotal));
        invoice.setTotalAmount(money(subtotal.add(invoice.getFineAmount()).subtract(invoice.getDiscount())));
        Invoice saved = invoiceRepository.save(invoice);
        auditLogService.log(currentUser().getId(), AuditAction.ISSUE_INVOICE.name());
        return toInvoice(saved);
    }

    private void addUtilityItem(Invoice invoice, Room room, User user, UtilityType type,
                                LocalDate periodStart, LocalDate periodEnd) {
        Optional<MeterReading> reading = readingRepository
                .findByMeterRoomIdAndMeterUtilityTypeAndBillingPeriod(room.getId(), type, periodStart);
        if (reading.isEmpty()) return;
        List<UtilityTariff> tariffs = tariffRepository.findEffective(type, periodStart);
        if (tariffs.isEmpty()) throw new BusinessException(ErrorCode.TARIFF_NOT_FOUND);

        List<RoomAssignment> residents = assignmentRepository.findResidentsForPeriod(room.getId(), periodStart, periodEnd);
        long totalResidentDays = residents.stream().mapToLong(item -> residentDays(item, periodStart, periodEnd)).sum();
        long userResidentDays = residents.stream().filter(item -> item.getUser().getId().equals(user.getId()))
                .mapToLong(item -> residentDays(item, periodStart, periodEnd)).sum();
        if (totalResidentDays <= 0 || userResidentDays <= 0) return;

        BigDecimal allocatedConsumption = reading.get().getConsumption()
                .multiply(BigDecimal.valueOf(userResidentDays))
                .divide(BigDecimal.valueOf(totalResidentDays), 3, RoundingMode.HALF_UP);
        String description = type == UtilityType.ELECTRICITY ? "Tiền điện phân bổ theo ngày ở" : "Tiền nước phân bổ theo ngày ở";
        addItem(invoice, type == UtilityType.ELECTRICITY ? InvoiceItemType.ELECTRICITY : InvoiceItemType.WATER,
                description, allocatedConsumption, tariffs.get(0).getUnitPrice());
    }

    private long residentDays(RoomAssignment assignment, LocalDate periodStart, LocalDate periodEnd) {
        LocalDate start = assignment.getStartDate().isAfter(periodStart) ? assignment.getStartDate() : periodStart;
        LocalDate assignmentEnd = assignment.getEndDate() == null ? periodEnd : assignment.getEndDate();
        LocalDate end = assignmentEnd.isBefore(periodEnd) ? assignmentEnd : periodEnd;
        return end.isBefore(start) ? 0 : ChronoUnit.DAYS.between(start, end) + 1;
    }

    private void addItem(Invoice invoice, InvoiceItemType type, String description, BigDecimal quantity, BigDecimal unitPrice) {
        BigDecimal normalizedQuantity = quantity.setScale(3, RoundingMode.HALF_UP);
        BigDecimal normalizedPrice = money(unitPrice);
        invoice.getItems().add(InvoiceItem.builder().id(UUID.randomUUID()).invoice(invoice).itemType(type)
                .description(description).quantity(normalizedQuantity).unitPrice(normalizedPrice)
                .amount(money(normalizedQuantity.multiply(normalizedPrice))).build());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceResponse> getInvoices(InvoiceStatus status, Pageable pageable) {
        Specification<Invoice> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        return invoiceRepository.findAll(spec, pageable).map(this::toInvoice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getMyInvoices() {
        return invoiceRepository.findByUserIdOrderByBillingPeriodDesc(currentUser().getId()).stream().map(this::toInvoice).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoice(UUID id) {
        Invoice invoice = invoiceRepository.findDetailedById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVOICE_NOT_FOUND));
        User actor = currentUser();
        if (actor.getRole() == UserRole.USER && !invoice.getUser().getId().equals(actor.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        return toInvoice(invoice);
    }

    @Override
    @Transactional
    public PaymentResponse recordCashPayment(CreatePaymentRequest request) {
        return createAndCompletePayment(request, PaymentMethod.CASH, null);
    }

    @Override
    @Transactional
    public VnPayUrlResponse createVnPayPayment(CreatePaymentRequest request, String ipAddress) {
        if (isBlank(vnPayProperties.getTmnCode()) || isBlank(vnPayProperties.getSecretKey())) {
            throw new BusinessException(ErrorCode.VNPAY_NOT_CONFIGURED);
        }
        Optional<Payment> duplicate = paymentRepository.findByIdempotencyKey(request.idempotencyKey());
        if (duplicate.isPresent()) {
            Payment payment = duplicate.get();
            return new VnPayUrlResponse(payment.getId(), payment.getPaymentCode(), buildVnPayUrl(payment, ipAddress));
        }
        Invoice invoice = invoiceRepository.findByIdForUpdate(request.invoiceId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVOICE_NOT_FOUND));
        User actor = currentUser();
        if (actor.getRole() == UserRole.USER && !invoice.getUser().getId().equals(actor.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        validatePaymentAmount(invoice, request.amount());
        Payment payment = paymentRepository.save(Payment.builder().id(UUID.randomUUID()).paymentCode(nextPaymentCode())
                .invoice(invoice).user(invoice.getUser()).amount(money(request.amount())).method(PaymentMethod.VNPAY)
                .status(PaymentStatus.PENDING).idempotencyKey(request.idempotencyKey()).build());
        return new VnPayUrlResponse(payment.getId(), payment.getPaymentCode(), buildVnPayUrl(payment, ipAddress));
    }

    @Override
    @Transactional
    public PaymentResponse handleVnPayCallback(Map<String, String> parameters) {
        if (!validVnPaySignature(parameters)) throw new BusinessException(ErrorCode.VNPAY_SIGNATURE_INVALID);
        Payment payment = paymentRepository.findByPaymentCode(parameters.get("vnp_TxnRef"))
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        String callbackAmount = parameters.get("vnp_Amount");
        String expectedAmount = payment.getAmount().movePointRight(2).setScale(0, RoundingMode.HALF_UP).toPlainString();
        if (!Objects.equals(vnPayProperties.getTmnCode(), parameters.get("vnp_TmnCode"))
                || !Objects.equals(expectedAmount, callbackAmount)) {
            throw new BusinessException(ErrorCode.VNPAY_SIGNATURE_INVALID);
        }
        if (!"00".equals(parameters.get("vnp_ResponseCode"))) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("VNPay response code: " + parameters.get("vnp_ResponseCode"));
            return toPayment(paymentRepository.save(payment));
        }
        return completePayment(payment, parameters.get("vnp_TransactionNo"));
    }

    private PaymentResponse createAndCompletePayment(CreatePaymentRequest request, PaymentMethod method, String externalRef) {
        Optional<Payment> duplicate = paymentRepository.findByIdempotencyKey(request.idempotencyKey());
        if (duplicate.isPresent()) return toPayment(duplicate.get());
        Invoice invoice = invoiceRepository.findByIdForUpdate(request.invoiceId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVOICE_NOT_FOUND));
        validatePaymentAmount(invoice, request.amount());
        Payment payment = paymentRepository.save(Payment.builder().id(UUID.randomUUID()).paymentCode(nextPaymentCode())
                .invoice(invoice).user(invoice.getUser()).amount(money(request.amount())).method(method)
                .status(PaymentStatus.PENDING).idempotencyKey(request.idempotencyKey()).build());
        return completePayment(payment, externalRef);
    }

    private PaymentResponse completePayment(Payment payment, String externalRef) {
        if (payment.getStatus() == PaymentStatus.SUCCESS) return toPayment(payment);
        Invoice invoice = invoiceRepository.findByIdForUpdate(payment.getInvoice().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVOICE_NOT_FOUND));
        validatePaymentAmount(invoice, payment.getAmount());
        invoice.setPaidAmount(money(invoice.getPaidAmount().add(payment.getAmount())));
        invoice.setStatus(invoice.getPaidAmount().compareTo(invoice.getTotalAmount()) == 0
                ? InvoiceStatus.PAID : InvoiceStatus.PARTIALLY_PAID);
        invoiceRepository.save(invoice);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setExternalReference(blankToNull(externalRef));
        payment.setPaidAt(Instant.now());
        Payment saved = paymentRepository.save(payment);
        auditLogService.log(payment.getUser().getId(), AuditAction.RECORD_PAYMENT.name());
        return toPayment(saved);
    }

    private void validatePaymentAmount(Invoice invoice, BigDecimal amount) {
        if (invoice.getStatus() == InvoiceStatus.PAID || invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.INVOICE_IMMUTABLE);
        }
        if (amount == null || amount.signum() <= 0) throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_INVALID);
        BigDecimal remaining = invoice.getTotalAmount().subtract(invoice.getPaidAmount());
        if (amount.compareTo(remaining) > 0) throw new BusinessException(ErrorCode.PAYMENT_OVERPAYMENT);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPayments() {
        return paymentRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toPayment).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getMyPayments() {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(currentUser().getId()).stream().map(this::toPayment).toList();
    }

    @Scheduled(cron = "0 15 0 * * *")
    @Transactional
    public void markOverdueInvoices() {
        invoiceRepository.findByStatusInAndDueDateBefore(
                List.of(InvoiceStatus.ISSUED, InvoiceStatus.PARTIALLY_PAID), LocalDate.now())
                .forEach(invoice -> invoice.setStatus(InvoiceStatus.OVERDUE));
    }

    private String buildVnPayUrl(Payment payment, String ipAddress) {
        ZoneId zone = ZoneId.of("Asia/Ho_Chi_Minh");
        ZonedDateTime now = ZonedDateTime.now(zone);
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", vnPayProperties.getVersion());
        params.put("vnp_Command", vnPayProperties.getCommand());
        params.put("vnp_TmnCode", vnPayProperties.getTmnCode());
        params.put("vnp_Amount", payment.getAmount().movePointRight(2).setScale(0, RoundingMode.HALF_UP).toPlainString());
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", payment.getPaymentCode());
        params.put("vnp_OrderInfo", "Thanh toan hoa don " + payment.getInvoice().getInvoiceCode());
        params.put("vnp_OrderType", vnPayProperties.getOrderType());
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", vnPayProperties.getReturnUrl());
        params.put("vnp_IpAddr", isBlank(ipAddress) ? "127.0.0.1" : ipAddress);
        params.put("vnp_CreateDate", now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        params.put("vnp_ExpireDate", now.plusMinutes(15).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        String query = queryString(params);
        return vnPayProperties.getVnpUrl() + "?" + query + "&vnp_SecureHash=" + hmacSha512(vnPayProperties.getSecretKey(), query);
    }

    private boolean validVnPaySignature(Map<String, String> parameters) {
        if (isBlank(vnPayProperties.getSecretKey())) return false;
        String received = parameters.get("vnp_SecureHash");
        Map<String, String> signed = new TreeMap<>(parameters);
        signed.remove("vnp_SecureHash");
        signed.remove("vnp_SecureHashType");
        return received != null && received.equalsIgnoreCase(hmacSha512(vnPayProperties.getSecretKey(), queryString(signed)));
    }

    private String queryString(Map<String, String> params) {
        return params.entrySet().stream().filter(entry -> !isBlank(entry.getValue()))
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .reduce((left, right) -> left + "&" + right).orElse("");
    }

    private String hmacSha512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            hmac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            return HexFormat.of().formatHex(hmac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể ký yêu cầu VNPay", ex);
        }
    }

    private String encode(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8); }

    private User currentUser() {
        return userRepository.findByUsername(CurrentUser.username())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private void validatePeriod(LocalDate period) {
        if (period == null || period.getDayOfMonth() != 1) throw new BusinessException(ErrorCode.INVALID_BILLING_PERIOD);
    }

    private String nextInvoiceCode() {
        String code;
        do { code = "HD-" + Year.now().getValue() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(); }
        while (invoiceRepository.existsByInvoiceCode(code));
        return code;
    }

    private String nextPaymentCode() {
        String code;
        do { code = "TT" + Year.now().getValue() + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase(); }
        while (paymentRepository.existsByPaymentCode(code));
        return code;
    }

    private MeterResponse toMeter(UtilityMeter meter) {
        return new MeterResponse(meter.getId(), meter.getRoom().getId(), meter.getRoom().getRoomNumber(),
                meter.getRoom().getBuilding().getCode(), meter.getMeterCode(), meter.getUtilityType(), meter.getUnit(), meter.getStatus());
    }

    private MeterReadingResponse toReading(MeterReading reading) {
        return new MeterReadingResponse(reading.getId(), reading.getMeter().getId(), reading.getMeter().getMeterCode(),
                reading.getMeter().getUtilityType(), reading.getBillingPeriod(), reading.getPreviousValue(),
                reading.getCurrentValue(), reading.getConsumption(), reading.isResetRecorded(), reading.getNote(), reading.getReadAt());
    }

    private TariffResponse toTariff(UtilityTariff tariff) {
        return new TariffResponse(tariff.getId(), tariff.getUtilityType(), tariff.getUnitPrice(),
                tariff.getEffectiveFrom(), tariff.getEffectiveTo(), tariff.isActive());
    }

    private InvoiceResponse toInvoice(Invoice invoice) {
        UserProfile profile = profileRepository.findById(invoice.getUser().getId()).orElse(null);
        List<InvoiceItemResponse> items = invoice.getItems().stream().map(item -> new InvoiceItemResponse(
                item.getId(), item.getItemType(), item.getDescription(), item.getQuantity(), item.getUnitPrice(), item.getAmount())).toList();
        return new InvoiceResponse(invoice.getId(), invoice.getInvoiceCode(), invoice.getUser().getId(),
                profile == null ? null : profile.getStudentCode(), profile == null ? invoice.getUser().getUsername() : profile.getFullName(),
                invoice.getContract().getId(), invoice.getContract().getContractCode(),
                invoice.getContract().getAssignment().getBed().getRoom().getRoomNumber(), invoice.getBillingPeriod(), invoice.getDueDate(),
                invoice.getSubtotal(), invoice.getDiscount(), invoice.getFineAmount(), invoice.getTotalAmount(), invoice.getPaidAmount(),
                invoice.getTotalAmount().subtract(invoice.getPaidAmount()), invoice.getStatus(), items);
    }

    private PaymentResponse toPayment(Payment payment) {
        return new PaymentResponse(payment.getId(), payment.getPaymentCode(), payment.getInvoice().getId(),
                payment.getInvoice().getInvoiceCode(), payment.getUser().getId(), payment.getAmount(), payment.getMethod(),
                payment.getStatus(), payment.getExternalReference(), payment.getPaidAt(), payment.getCreatedAt());
    }

    private BigDecimal money(BigDecimal value) { return orZero(value).setScale(2, RoundingMode.HALF_UP); }
    private BigDecimal orZero(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
    private String blankToNull(String value) { return isBlank(value) ? null : value.trim(); }
    private boolean isBlank(String value) { return value == null || value.isBlank(); }
}

