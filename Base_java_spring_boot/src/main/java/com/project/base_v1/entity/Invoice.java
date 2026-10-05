package com.project.base_v1.entity;

import com.project.base_v1.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "invoices")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@SQLRestriction("deleted_at IS NULL")
public class Invoice extends BaseAuditEntity {
    @Id UUID id;
    @Column(name = "invoice_code", nullable = false, unique = true, length = 40) String invoiceCode;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) User user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "contract_id", nullable = false) Contract contract;
    @Column(name = "billing_period", nullable = false) LocalDate billingPeriod;
    @Column(name = "due_date", nullable = false) LocalDate dueDate;
    @Builder.Default @Column(nullable = false, precision = 15, scale = 2) BigDecimal subtotal = BigDecimal.ZERO;
    @Builder.Default @Column(nullable = false, precision = 15, scale = 2) BigDecimal discount = BigDecimal.ZERO;
    @Builder.Default @Column(name = "fine_amount", nullable = false, precision = 15, scale = 2) BigDecimal fineAmount = BigDecimal.ZERO;
    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2) BigDecimal totalAmount;
    @Builder.Default @Column(name = "paid_amount", nullable = false, precision = 15, scale = 2) BigDecimal paidAmount = BigDecimal.ZERO;
    @Builder.Default @Enumerated(EnumType.STRING) @Column(nullable = false) InvoiceStatus status = InvoiceStatus.ISSUED;
    @Column(name = "issued_at", nullable = false) Instant issuedAt;
    @Builder.Default @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true) List<InvoiceItem> items = new ArrayList<>();
}

