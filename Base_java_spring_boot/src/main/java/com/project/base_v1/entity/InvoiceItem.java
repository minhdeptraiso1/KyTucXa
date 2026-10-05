package com.project.base_v1.entity;

import com.project.base_v1.enums.InvoiceItemType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "invoice_items")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@SQLRestriction("deleted_at IS NULL")
public class InvoiceItem extends BaseAuditEntity {
    @Id UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "invoice_id", nullable = false) Invoice invoice;
    @Enumerated(EnumType.STRING) @Column(name = "item_type", nullable = false) InvoiceItemType itemType;
    @Column(nullable = false, length = 255) String description;
    @Column(nullable = false, precision = 15, scale = 3) BigDecimal quantity;
    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2) BigDecimal unitPrice;
    @Column(nullable = false, precision = 15, scale = 2) BigDecimal amount;
}

