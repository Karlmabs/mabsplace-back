package com.mabsplace.mabsplaceback.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "digital_products")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DigitalProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private ProductCategory category;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "base_currency", nullable = false, length = 3)
    private String baseCurrency; // EUR, USD, etc.

    @Column(name = "min_amount")
    private BigDecimal minAmount; // Montant minimum

    @Column(name = "max_amount")
    private BigDecimal maxAmount; // Montant maximum

    @Column(name = "fixed_price", precision = 19, scale = 2)
    private BigDecimal fixedPrice; // Prix fixe en XAF (nouveau modèle)

    @Column(name = "profit_margin", precision = 19, scale = 2)
    private BigDecimal profitMargin; // Marge bénéficiaire de Mabsplace en XAF

    @Column(name = "custom_input_fields", columnDefinition = "TEXT")
    private String customInputFields; // Champs de saisie personnalisés (JSON)

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at")
    private java.util.Date createdAt;

    @Column(name = "updated_at")
    private java.util.Date updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new java.util.Date();
        updatedAt = new java.util.Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new java.util.Date();
    }
}
