package com.mabsplace.mabsplaceback.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "digital_goods_orders")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DigitalGoodsOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private DigitalProduct product;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount; // Montant demandé (ex: 10, 25, 50)

    // Prix et calculs
    @Column(name = "base_currency", nullable = false, length = 3)
    private String baseCurrency; // EUR, USD

    @Column(name = "base_currency_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal baseCurrencyPrice; // Prix en devise originale

    @Column(name = "exchange_rate", nullable = false, precision = 19, scale = 6)
    private BigDecimal exchangeRate; // Taux de change utilisé

    @Column(name = "converted_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal convertedPrice; // Prix converti en XAF

    @Column(name = "service_fee", nullable = false, precision = 19, scale = 2)
    private BigDecimal serviceFee; // Frais de service en XAF

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount; // Total final en XAF (convertedPrice + serviceFee)

    @Column(precision = 19, scale = 2)
    private BigDecimal profit; // Notre marge/profit

    // Statut et paiement
    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @Column(name = "payment_method", nullable = false)
    @Builder.Default
    private String paymentMethod = "WALLET";

    @Column(name = "transaction_id")
    private Long transactionId;

    // Livraison
    @Column(name = "delivery_info", columnDefinition = "TEXT")
    private String deliveryInfo; // Codes/credentials fournis par l'admin

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @ManyToOne
    @JoinColumn(name = "delivered_by")
    private User deliveredBy; // Admin qui a livré

    // Dates
    @Column(name = "created_at")
    private java.util.Date createdAt;

    @Column(name = "updated_at")
    private java.util.Date updatedAt;

    @Column(name = "paid_at")
    private java.util.Date paidAt;

    @Column(name = "delivered_at")
    private java.util.Date deliveredAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new java.util.Date();
        updatedAt = new java.util.Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new java.util.Date();
    }

    public enum OrderStatus {
        PENDING,      // En attente de paiement
        PAID,         // Payé, en attente de traitement
        PROCESSING,   // En cours de traitement par l'admin
        DELIVERED,    // Livré
        CANCELLED,    // Annulé
        REFUNDED      // Remboursé
    }
}
