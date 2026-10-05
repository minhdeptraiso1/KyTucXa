package com.project.base_v1.service;

import com.project.base_v1.config.VnPayProperties;
import com.project.base_v1.dto.request.billing.CreateMeterReadingRequest;
import com.project.base_v1.dto.request.billing.CreatePaymentRequest;
import com.project.base_v1.dto.request.billing.GenerateInvoiceRequest;
import com.project.base_v1.dto.response.billing.InvoiceResponse;
import com.project.base_v1.dto.response.billing.VnPayUrlResponse;
import com.project.base_v1.entity.*;
import com.project.base_v1.enums.*;
import com.project.base_v1.exception.BusinessException;
import com.project.base_v1.exception.ErrorCode;
import com.project.base_v1.repository.*;
import com.project.base_v1.service.impl.BillingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingServiceTest {
    @Mock UtilityMeterRepository meterRepository;
    @Mock MeterReadingRepository readingRepository;
    @Mock UtilityTariffRepository tariffRepository;
    @Mock InvoiceRepository invoiceRepository;
    @Mock PaymentRepository paymentRepository;
    @Mock RoomRepository roomRepository;
    @Mock ContractRepository contractRepository;
    @Mock RoomAssignmentRepository assignmentRepository;
    @Mock UserRepository userRepository;
    @Mock UserProfileRepository profileRepository;
    @Mock AuditLogService auditLogService;
    VnPayProperties properties;

    @BeforeEach
    void setup() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("staff", null));
        properties = new VnPayProperties();
    }

    @Test
    void recordReading_rejectsValueLowerThanPreviousWithoutReset() {
        UUID meterId = UUID.randomUUID();
        UtilityMeter meter = UtilityMeter.builder().id(meterId).build();
        MeterReading latest = MeterReading.builder().billingPeriod(LocalDate.of(2026, 9, 1))
                .currentValue(new BigDecimal("120.000")).build();
        when(meterRepository.findById(meterId)).thenReturn(Optional.of(meter));
        when(readingRepository.findFirstByMeterIdOrderByBillingPeriodDesc(meterId)).thenReturn(Optional.of(latest));

        BusinessException exception = assertThrows(BusinessException.class, () -> service().recordReading(
                new CreateMeterReadingRequest(meterId, LocalDate.of(2026, 10, 1), new BigDecimal("119"), false, null)));

        assertEquals(ErrorCode.INVALID_METER_READING, exception.getErrorCode());
        verify(readingRepository, never()).save(any());
    }

    @Test
    void directPayment_rejectsOverpayment() {
        UUID invoiceId = UUID.randomUUID();
        Invoice invoice = Invoice.builder().id(invoiceId).totalAmount(new BigDecimal("100000"))
                .paidAmount(new BigDecimal("90000")).status(InvoiceStatus.PARTIALLY_PAID).build();
        when(paymentRepository.findByIdempotencyKey("key-1")).thenReturn(Optional.empty());
        when(invoiceRepository.findByIdForUpdate(invoiceId)).thenReturn(Optional.of(invoice));

        BusinessException exception = assertThrows(BusinessException.class, () -> service().recordCashPayment(
                new CreatePaymentRequest(invoiceId, new BigDecimal("20000"), "key-1")));

        assertEquals(ErrorCode.PAYMENT_OVERPAYMENT, exception.getErrorCode());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void createVnPayPayment_buildsSignedSandboxRedirectUrl() {
        UUID invoiceId = UUID.randomUUID();
        User student = User.builder().id(UUID.randomUUID()).username("student").role(UserRole.USER).build();
        User staff = User.builder().id(UUID.randomUUID()).username("staff").role(UserRole.STAFF).build();
        Invoice invoice = Invoice.builder().id(invoiceId).invoiceCode("HD-TEST-01").user(student)
                .totalAmount(new BigDecimal("100000")).paidAmount(BigDecimal.ZERO).status(InvoiceStatus.ISSUED).build();
        properties.setVnpUrl("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
        properties.setTmnCode("TESTCODE");
        properties.setSecretKey("TESTSECRETKEY12345678901234567890");
        properties.setReturnUrl("http://localhost:5173/payment/vnpay-return");
        properties.setOrderType("other");
        when(paymentRepository.findByIdempotencyKey("vnpay-key")).thenReturn(Optional.empty());
        when(invoiceRepository.findByIdForUpdate(invoiceId)).thenReturn(Optional.of(invoice));
        when(userRepository.findByUsername("staff")).thenReturn(Optional.of(staff));
        when(paymentRepository.existsByPaymentCode(anyString())).thenReturn(false);
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        VnPayUrlResponse response = service().createVnPayPayment(
                new CreatePaymentRequest(invoiceId, new BigDecimal("100000"), "vnpay-key"), "127.0.0.1");

        assertTrue(response.paymentCode().matches("[A-Z0-9]+"));
        assertTrue(response.paymentUrl().startsWith("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?"));
        assertTrue(response.paymentUrl().contains("vnp_TmnCode=TESTCODE"));
        assertTrue(response.paymentUrl().contains("vnp_Amount=10000000"));
        assertTrue(response.paymentUrl().contains("vnp_SecureHash="));
    }

    @Test
    void generateInvoice_calculatesRoomServiceFineAndDiscount() {
        LocalDate period = LocalDate.of(2026, 10, 1);
        User student = User.builder().id(UUID.randomUUID()).username("student").email("student@ktx.local").role(UserRole.USER).build();
        User staff = User.builder().id(UUID.randomUUID()).username("staff").email("staff@ktx.local").role(UserRole.STAFF).build();
        Building building = Building.builder().id(UUID.randomUUID()).code("A").build();
        Room room = Room.builder().id(UUID.randomUUID()).building(building).roomNumber("A101").capacity(8).build();
        Bed bed = Bed.builder().id(UUID.randomUUID()).room(room).bedNumber("01").build();
        RoomAssignment assignment = RoomAssignment.builder().id(UUID.randomUUID()).user(student).bed(bed)
                .startDate(period).status(AssignmentStatus.ACTIVE).build();
        Contract contract = Contract.builder().id(UUID.randomUUID()).contractCode("KTX-TEST").user(student).assignment(assignment)
                .startDate(period).endDate(period.withDayOfMonth(31)).rentalPrice(new BigDecimal("600000")).status(ContractStatus.ACTIVE).build();
        when(contractRepository.findById(contract.getId())).thenReturn(Optional.of(contract));
        when(invoiceRepository.existsByContractIdAndBillingPeriod(contract.getId(), period)).thenReturn(false);
        when(readingRepository.findByMeterRoomIdAndMeterUtilityTypeAndBillingPeriod(eq(room.getId()), any(), eq(period)))
                .thenReturn(Optional.empty());
        when(invoiceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findByUsername("staff")).thenReturn(Optional.of(staff));
        when(profileRepository.findById(student.getId())).thenReturn(Optional.empty());

        InvoiceResponse response = service().generateInvoice(new GenerateInvoiceRequest(
                contract.getId(), period, LocalDate.of(2026, 10, 10),
                new BigDecimal("100000"), new BigDecimal("20000"), new BigDecimal("50000")));

        assertEquals(new BigDecimal("700000.00"), response.subtotal());
        assertEquals(new BigDecimal("730000.00"), response.totalAmount());
        assertEquals(3, response.items().size());
    }

    private BillingServiceImpl service() {
        return new BillingServiceImpl(meterRepository, readingRepository, tariffRepository, invoiceRepository,
                paymentRepository, roomRepository, contractRepository, assignmentRepository, userRepository,
                profileRepository, auditLogService, properties);
    }
}

