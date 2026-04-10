package com.tendanz.pricing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Entity representing pricing rules for a product.
 * Contains base rate and age factor multipliers for different age categories.
 */
@Entity
@Table(name = "pricing_rule", indexes = {
        @Index(name = "idx_pricing_rule_product_id", columnList = "product_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "base_rate", nullable = false)
    private BigDecimal baseRate;

    @Column(name = "age_factor_young", nullable = false)
    private BigDecimal ageFactorYoung;

    @Column(name = "age_factor_adult", nullable = false)
    private BigDecimal ageFactorAdult;

    @Column(name = "age_factor_senior", nullable = false)
    private BigDecimal ageFactorSenior;

    @Column(name = "age_factor_elderly", nullable = false)
    private BigDecimal ageFactorElderly;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
