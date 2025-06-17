package com.mabsplace.mabsplaceback.utils;

import com.mabsplace.mabsplaceback.domain.entities.*;
import com.mabsplace.mabsplaceback.domain.enums.*;
import com.mabsplace.mabsplaceback.domain.dtos.user.UserRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.payment.PaymentRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.myService.MyServiceRequestDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;

/**
 * Utility class for building test data objects.
 * Provides builder methods for creating test entities and DTOs with sensible defaults.
 */
public class TestDataBuilder {

    // User builders
    public static User.UserBuilder createUserBuilder() {
        return User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .authType(AuthenticationType.DATABASE)
                .referralCode("REF123");
    }

    public static User createTestUser() {
        return createUserBuilder().build();
    }

    public static UserRequestDto createUserRequestDto() {
        UserRequestDto dto = new UserRequestDto();
        dto.setUsername("testuser");
        dto.setEmail("test@example.com");
        dto.setPassword("password123");
        return dto;
    }

    // Role builders
    public static Role.RoleBuilder createRoleBuilder() {
        return Role.builder()
                .name("ROLE_USER")
                .code("USER")
                .description("Standard user role");
    }

    public static Role createTestRole() {
        return createRoleBuilder().build();
    }

    // Currency builders
    public static Currency.CurrencyBuilder createCurrencyBuilder() {
        return Currency.builder()
                .name("USD")
                .symbol("$")
                .exchangeRate(1.0);
    }

    public static Currency createTestCurrency() {
        return createCurrencyBuilder().build();
    }

    // Service builders
    public static MyService createTestService() {
        MyService service = new MyService();
        service.setName("Test Service");
        service.setDescription("Test service description");
        return service;
    }

    public static MyServiceRequestDto createServiceRequestDto() {
        MyServiceRequestDto dto = new MyServiceRequestDto();
        dto.setName("Test Service");
        dto.setDescription("Test service description");
        return dto;
    }

    // Payment builders
    public static Payment createTestPayment() {
        Payment payment = new Payment();
        payment.setAmount(new BigDecimal("100.00"));
        payment.setPaymentDate(new Date());
        payment.setStatus(PaymentStatus.PAID);
        return payment;
    }

    public static PaymentRequestDto createPaymentRequestDto() {
        return PaymentRequestDto.builder()
                .amount(new BigDecimal("100.00"))
                .userId(1L)
                .currencyId(1L)
                .serviceId(1L)
                .subscriptionPlanId(1L)
                .paymentDate(new Date())
                .build();
    }

    // Subscription builders
    public static Subscription.SubscriptionBuilder createSubscriptionBuilder() {
        return Subscription.builder()
                .startDate(new Date())
                .endDate(new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)) // 30 days from now
                .status(SubscriptionStatus.ACTIVE);
    }

    public static Subscription createTestSubscription() {
        return createSubscriptionBuilder().build();
    }

    // SubscriptionPlan builders
    public static SubscriptionPlan createTestSubscriptionPlan() {
        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setName("Basic Plan");
        plan.setDescription("Basic subscription plan");
        plan.setPrice(new BigDecimal("29.99"));
        plan.setPeriod(Period.MONTHLY);
        return plan;
    }

    // Expense builders
    public static Expense createTestExpense() {
        Expense expense = new Expense();
        expense.setDescription("Test expense");
        expense.setAmount(new BigDecimal("50.00"));
        expense.setExpenseDate(LocalDateTime.now());
        expense.setRecurring(false);
        return expense;
    }

    // ExpenseCategory builders
    public static ExpenseCategory createTestExpenseCategory() {
        ExpenseCategory category = new ExpenseCategory();
        category.setName("Office Supplies");
        category.setDescription("Office supplies and equipment");
        return category;
    }

    // PromoCode builders
    public static PromoCode.PromoCodeBuilder createPromoCodeBuilder() {
        return PromoCode.builder()
                .code("PROMO123")
                .discountAmount(new BigDecimal("10.00"))
                .maxUsage(100)
                .usedCount(0)
                .status(PromoCodeStatus.ACTIVE);
    }

    public static PromoCode createTestPromoCode() {
        return createPromoCodeBuilder().build();
    }

    // Transaction builders
    public static Transaction.TransactionBuilder createTransactionBuilder() {
        return Transaction.builder()
                .transactionRef("TXN123456")
                .amount(new BigDecimal("100.00"))
                .transactionDate(new Date())
                .transactionStatus(TransactionStatus.COMPLETED)
                .transactionType(TransactionType.TOPUP);
    }

    public static Transaction createTestTransaction() {
        return createTransactionBuilder().build();
    }
}
