package com.example.stormgate_product_service.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "product_variants", indexes = {
    @Index(name = "idx_product_id", columnList = "product_id"),
    @Index(name = "idx_variant_sku", columnList = "sku", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID productId;

    @Column(unique = true, length = 100)
    private String sku;

    @Column(length = 255)
    private String variantName;

    @Column
    private BigDecimal price;

    @Column
    private Integer stock;
}
