package com.mabsplace.mabsplaceback.domain.controllers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/stats")
    public DashboardStats getStats() {
        // Get total subscribers (count of active subscriptions)
        Integer totalSubscribers = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT user_id) FROM subscriptions WHERE status = 'ACTIVE'",
                Integer.class
        );

        // Get monthly revenue (sum of payments in current month)
        Double monthlyRevenue = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(amount), 0) FROM payments " +
                        "WHERE MONTH(payment_date) = MONTH(CURRENT_DATE) " +
                        "AND YEAR(payment_date) = YEAR(CURRENT_DATE) " +
                        "AND status = 'PAID'",
                Double.class
        );

        // Get average subscription value
        Double avgValue = jdbcTemplate.queryForObject(
                "SELECT AVG(amount) FROM payments WHERE status = 'PAID'",
                Double.class
        );

        // Get active subscriptions count
        Integer activeSubscriptions = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM subscriptions WHERE status = 'ACTIVE'",
                Integer.class
        );

        // Calculate growth percentages (comparing with previous month)
        Double subscriptionGrowth = calculateGrowthRate("subscriptions");
        Double revenueGrowth = calculateGrowthRate("payments");

        return new DashboardStats(
                totalSubscribers,
                monthlyRevenue,
                avgValue,
                activeSubscriptions,
                subscriptionGrowth,
                revenueGrowth
        );
    }

    @GetMapping("/revenue-trend")
    public List<MonthlyRevenue> getRevenueAndSubscriptionsTrend() {
        return jdbcTemplate.query(
                "SELECT DATE_FORMAT(p.payment_date, '%b') as month, " +
                        "SUM(p.amount) as revenue, " +
                        "COUNT(DISTINCT s.id) as subscriptions " +
                        "FROM payments p " +
                        "LEFT JOIN subscriptions s ON MONTH(p.payment_date) = MONTH(s.start_date) " +
                        "WHERE p.status = 'PAID' " +
                        "AND p.payment_date >= DATE_SUB(CURRENT_DATE, INTERVAL 6 MONTH) " +
                        "GROUP BY MONTH(p.payment_date) " +
                        "ORDER BY p.payment_date",
                (rs, rowNum) -> new MonthlyRevenue(
                        rs.getString("month"),
                        rs.getDouble("revenue"),
                        rs.getInt("subscriptions")
                )
        );
    }

    @GetMapping("/service-distribution")
    public List<ServiceDistribution> getServiceDistribution() {
        return jdbcTemplate.query(
                "SELECT s.name, COUNT(sub.id) as value, s.logo as color " +
                        "FROM services s " +
                        "LEFT JOIN subscriptions sub ON s.id = sub.service_id " +
                        "WHERE sub.status = 'ACTIVE' " +
                        "GROUP BY s.id, s.name",
                (rs, rowNum) -> new ServiceDistribution(
                        rs.getString("name"),
                        rs.getInt("value"),
                        rs.getString("color")
                )
        );
    }

    @GetMapping("/top-services")
    public List<TopService> getTopServices() {
        return jdbcTemplate.query(
                "SELECT s.name, " +
                        "COUNT(sub.id) as subscribers, " +
                        "((COUNT(sub.id) - LAG(COUNT(sub.id)) OVER (PARTITION BY s.id ORDER BY MONTH(sub.start_date))) " +
                        "/ LAG(COUNT(sub.id)) OVER (PARTITION BY s.id ORDER BY MONTH(sub.start_date)) * 100) as growth " +
                        "FROM services s " +
                        "LEFT JOIN subscriptions sub ON s.id = sub.service_id " +
                        "WHERE sub.status = 'ACTIVE' " +
                        "GROUP BY s.id, s.name " +
                        "ORDER BY subscribers DESC " +
                        "LIMIT 4",
                (rs, rowNum) -> new TopService(
                        rs.getString("name"),
                        rs.getInt("subscribers"),
                        rs.getDouble("growth")
                )
        );
    }

    private Double calculateGrowthRate(String table) {
        String query = "";
        if (table.equals("subscriptions")) {
            query = "SELECT " +
                    "((SELECT COUNT(*) FROM subscriptions WHERE status = 'ACTIVE' AND MONTH(start_date) = MONTH(CURRENT_DATE)) - " +
                    "(SELECT COUNT(*) FROM subscriptions WHERE status = 'ACTIVE' AND MONTH(start_date) = MONTH(DATE_SUB(CURRENT_DATE, INTERVAL 1 MONTH)))) / " +
                    "(SELECT COUNT(*) FROM subscriptions WHERE status = 'ACTIVE' AND MONTH(start_date) = MONTH(DATE_SUB(CURRENT_DATE, INTERVAL 1 MONTH))) * 100";
        } else {
            query = "SELECT " +
                    "((SELECT COALESCE(SUM(amount), 0) FROM payments WHERE status = 'PAID' AND MONTH(payment_date) = MONTH(CURRENT_DATE)) - " +
                    "(SELECT COALESCE(SUM(amount), 0) FROM payments WHERE status = 'PAID' AND MONTH(payment_date) = MONTH(DATE_SUB(CURRENT_DATE, INTERVAL 1 MONTH)))) / " +
                    "(SELECT COALESCE(SUM(amount), 0) FROM payments WHERE status = 'PAID' AND MONTH(payment_date) = MONTH(DATE_SUB(CURRENT_DATE, INTERVAL 1 MONTH))) * 100";
        }
        return jdbcTemplate.queryForObject(query, Double.class);
    }
}

// Data Transfer Objects
@Data
@NoArgsConstructor
@AllArgsConstructor
class DashboardStats {
    private Integer totalSubscribers;
    private Double monthlyRevenue;
    private Double avgSubscriptionValue;
    private Integer activeSubscriptions;
    private Double subscriptionGrowth;
    private Double revenueGrowth;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class MonthlyRevenue {
    private String month;
    private Double revenue;
    private Integer subscriptions;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class ServiceDistribution {
    private String name;
    private Integer value;
    private String color;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class TopService {
    private String name;
    private Integer subscribers;
    private Double growth;
}