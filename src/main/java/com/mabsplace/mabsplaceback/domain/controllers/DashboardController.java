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

        // New expense stats
        Double monthlyExpenses = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(amount), 0) FROM expenses " +
                        "WHERE MONTH(expense_date) = MONTH(CURRENT_DATE) " +
                        "AND YEAR(expense_date) = YEAR(CURRENT_DATE)",
                Double.class
        );

        // Calculate net profit
        Double netProfit = (monthlyRevenue != null ? monthlyRevenue : 0) - (monthlyExpenses != null ? monthlyExpenses : 0);


        // Calculate profit margin
        Double profitMargin = (monthlyRevenue != null && monthlyRevenue > 0) ? (netProfit / monthlyRevenue) * 100 : 0;

        return new DashboardStats(
                totalSubscribers,
                monthlyRevenue,
                avgValue,
                activeSubscriptions,
                subscriptionGrowth,
                revenueGrowth,
                monthlyExpenses,
                netProfit,
                profitMargin
        );
    }

    @GetMapping("/expense-trends")
    public List<ExpenseTrend> getExpenseTrends() {
        return jdbcTemplate.query(
                """
                        WITH RECURSIVE Months AS (
                            SELECT DATE_SUB(CURRENT_DATE, INTERVAL 5 MONTH) as date
                            UNION ALL
                            SELECT DATE_ADD(date, INTERVAL 1 MONTH)
                            FROM Months
                            WHERE date < CURRENT_DATE
                        )
                        SELECT 
                            DATE_FORMAT(m.date, '%b') as month,
                            COALESCE(SUM(e.amount), 0) as total_expenses,
                            COALESCE(SUM(CASE WHEN e.is_recurring = TRUE THEN e.amount ELSE 0 END), 0) as recurring_expenses,
                            COALESCE(SUM(CASE WHEN e.is_recurring = FALSE THEN e.amount ELSE 0 END), 0) as one_time_expenses
                        FROM Months m
                        LEFT JOIN expenses e ON 
                            MONTH(e.expense_date) = MONTH(m.date) 
                            AND YEAR(e.expense_date) = YEAR(m.date)
                        GROUP BY m.date
                        ORDER BY m.date
                        """,
                (rs, rowNum) -> new ExpenseTrend(
                        rs.getString("month"),
                        rs.getDouble("total_expenses"),
                        rs.getDouble("recurring_expenses"),
                        rs.getDouble("one_time_expenses")
                )
        );
    }

    @GetMapping("/expense-by-category")
    public List<ExpenseByCategory> getExpenseByCategory() {
        return jdbcTemplate.query(
                """
                        SELECT 
                            ec.name as category,
                            COALESCE(SUM(e.amount), 0) as amount,
                            COUNT(e.id) as transaction_count
                        FROM expense_categories ec
                        LEFT JOIN expenses e ON ec.id = e.category_id
                        WHERE MONTH(e.expense_date) = MONTH(CURRENT_DATE)
                        AND YEAR(e.expense_date) = YEAR(CURRENT_DATE)
                        GROUP BY ec.id, ec.name
                        ORDER BY amount DESC
                        """,
                (rs, rowNum) -> new ExpenseByCategory(
                        rs.getString("category"),
                        rs.getDouble("amount"),
                        rs.getInt("transaction_count")
                )
        );
    }

    @GetMapping("/profitability-metrics")
    public ProfitabilityMetrics getProfitabilityMetrics() {
        return jdbcTemplate.queryForObject(
                """
                        WITH MonthlyMetrics AS (
                            SELECT
                                COALESCE(SUM(p.amount), 0) as revenue,
                                COALESCE((
                                    SELECT SUM(e.amount)
                                    FROM expenses e
                                    WHERE MONTH(e.expense_date) = MONTH(CURRENT_DATE)
                                    AND YEAR(e.expense_date) = YEAR(CURRENT_DATE)
                                ), 0) as expenses
                            FROM payments p
                            WHERE p.status = 'PAID'
                            AND MONTH(p.payment_date) = MONTH(CURRENT_DATE)
                            AND YEAR(p.payment_date) = YEAR(CURRENT_DATE)
                        )
                        SELECT
                            revenue,
                            expenses,
                            (revenue - expenses) as net_profit,
                            CASE 
                                WHEN revenue > 0 THEN ((revenue - expenses) / revenue * 100)
                                ELSE 0 
                            END as profit_margin
                        FROM MonthlyMetrics
                        """,
                (rs, rowNum) -> new ProfitabilityMetrics(
                        rs.getDouble("revenue"),
                        rs.getDouble("expenses"),
                        rs.getDouble("net_profit"),
                        rs.getDouble("profit_margin")
                )
        );
    }

    @GetMapping("/revenue-trend")
    public List<MonthlyRevenue> getRevenueAndSubscriptionsTrend() {
        String sql = """
                    WITH RECURSIVE Months AS (
                        SELECT DATE_SUB(CURRENT_DATE, INTERVAL 5 MONTH) as date
                        UNION ALL
                        SELECT DATE_ADD(date, INTERVAL 1 MONTH)
                        FROM Months
                        WHERE date < CURRENT_DATE
                    )
                    SELECT 
                        DATE_FORMAT(m.date, '%b') as month,
                        COALESCE(SUM(p.amount), 0) as revenue,
                        COALESCE(COUNT(DISTINCT s.id), 0) as subscriptions
                    FROM Months m
                    LEFT JOIN payments p ON 
                        MONTH(p.payment_date) = MONTH(m.date) 
                        AND YEAR(p.payment_date) = YEAR(m.date)
                        AND p.status = 'PAID'
                    LEFT JOIN subscriptions s ON 
                        MONTH(s.start_date) = MONTH(m.date)
                        AND YEAR(s.start_date) = YEAR(m.date)
                        AND s.status = 'ACTIVE'
                    GROUP BY m.date
                    ORDER BY m.date
                """;

        return jdbcTemplate.query(sql,
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
                "SELECT s.name, COUNT(sub.id) as value " +
                        "FROM services s " +
                        "LEFT JOIN subscriptions sub ON s.id = sub.service_id " +
                        "WHERE sub.status = 'ACTIVE' " +
                        "GROUP BY s.id, s.name",
                (rs, rowNum) -> new ServiceDistribution(
                        rs.getString("name"),
                        rs.getInt("value"),
                        getRandomColor()
                )
        );
    }

    private String getRandomColor() {
        Random random = new Random();
        int r = random.nextInt(256);
        int g = random.nextInt(256);
        int b = random.nextInt(256);
        return String.format("#%02x%02x%02x", r, g, b);
    }

    @GetMapping("/top-services")
    public List<TopService> getTopServices() {
        String sql = """
                    WITH CurrentMonthStats AS (
                        SELECT 
                            s.id,
                            s.name,
                            COUNT(DISTINCT sub.id) as current_subscribers
                        FROM services s
                        LEFT JOIN subscriptions sub ON s.id = sub.service_id
                        WHERE sub.status = 'ACTIVE'
                        AND MONTH(sub.start_date) = MONTH(CURRENT_DATE)
                        AND YEAR(sub.start_date) = YEAR(CURRENT_DATE)
                        GROUP BY s.id, s.name
                    ),
                    LastMonthStats AS (
                        SELECT 
                            s.id,
                            COUNT(DISTINCT sub.id) as last_month_subscribers
                        FROM services s
                        LEFT JOIN subscriptions sub ON s.id = sub.service_id
                        WHERE sub.status = 'ACTIVE'
                        AND MONTH(sub.start_date) = MONTH(DATE_SUB(CURRENT_DATE, INTERVAL 1 MONTH))
                        AND YEAR(sub.start_date) = YEAR(DATE_SUB(CURRENT_DATE, INTERVAL 1 MONTH))
                        GROUP BY s.id
                    )
                    SELECT 
                        cms.name,
                        cms.current_subscribers as subscribers,
                        CASE 
                            WHEN lms.last_month_subscribers > 0 
                            THEN ((cms.current_subscribers - lms.last_month_subscribers) / lms.last_month_subscribers * 100)
                            ELSE 0 
                        END as growth
                    FROM CurrentMonthStats cms
                    LEFT JOIN LastMonthStats lms ON cms.id = lms.id
                    ORDER BY cms.current_subscribers DESC
                    LIMIT 4
                """;

        return jdbcTemplate.query(sql,
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
                    "CASE WHEN (SELECT COUNT(*) FROM subscriptions WHERE status = 'ACTIVE' AND MONTH(start_date) = MONTH(DATE_SUB(CURRENT_DATE, INTERVAL 1 MONTH))) = 0 " +
                    "THEN 0 ELSE " +
                    "((SELECT COUNT(*) FROM subscriptions WHERE status = 'ACTIVE' AND MONTH(start_date) = MONTH(CURRENT_DATE)) - " +
                    "(SELECT COUNT(*) FROM subscriptions WHERE status = 'ACTIVE' AND MONTH(start_date) = MONTH(DATE_SUB(CURRENT_DATE, INTERVAL 1 MONTH)))) / " +
                    "(SELECT COUNT(*) FROM subscriptions WHERE status = 'ACTIVE' AND MONTH(start_date) = MONTH(DATE_SUB(CURRENT_DATE, INTERVAL 1 MONTH))) * 100 END";
        } else {
            query = "SELECT " +
                    "CASE WHEN (SELECT COALESCE(SUM(amount), 0) FROM payments WHERE status = 'PAID' AND MONTH(payment_date) = MONTH(DATE_SUB(CURRENT_DATE, INTERVAL 1 MONTH))) = 0 " +
                    "THEN 0 ELSE " +
                    "((SELECT COALESCE(SUM(amount), 0) FROM payments WHERE status = 'PAID' AND MONTH(payment_date) = MONTH(CURRENT_DATE)) - " +
                    "(SELECT COALESCE(SUM(amount), 0) FROM payments WHERE status = 'PAID' AND MONTH(payment_date) = MONTH(DATE_SUB(CURRENT_DATE, INTERVAL 1 MONTH)))) / " +
                    "(SELECT COALESCE(SUM(amount), 0) FROM payments WHERE status = 'PAID' AND MONTH(payment_date) = MONTH(DATE_SUB(CURRENT_DATE, INTERVAL 1 MONTH))) * 100 END";
        }
        return jdbcTemplate.queryForObject(query, Double.class);
    }

    @GetMapping("/historical-metrics")
    public HistoricalMetrics getHistoricalMetrics() {
        // Get yearly data
        List<YearlyMetric> yearlyMetrics = jdbcTemplate.query(
                """
                WITH YearlyData AS (
                    SELECT 
                        YEAR(payment_date) as year,
                        SUM(amount) as revenue
                    FROM payments
                    WHERE status = 'PAID'
                    GROUP BY YEAR(payment_date)
                ),
                YearlyExpenses AS (
                    SELECT 
                        YEAR(expense_date) as year,
                        SUM(amount) as expenses
                    FROM expenses
                    GROUP BY YEAR(expense_date)
                )
                SELECT 
                    yd.year,
                    yd.revenue,
                    COALESCE(ye.expenses, 0) as expenses,
                    (yd.revenue - COALESCE(ye.expenses, 0)) as net_profit
                FROM YearlyData yd
                LEFT JOIN YearlyExpenses ye ON yd.year = ye.year
                ORDER BY yd.year DESC
                LIMIT 3
                """,
                (rs, rowNum) -> new YearlyMetric(
                        rs.getInt("year"),
                        rs.getDouble("revenue"),
                        rs.getDouble("expenses"),
                        rs.getDouble("net_profit")
                )
        );

        // Get service performance history
        List<ServiceHistoricalMetric> serviceMetrics = jdbcTemplate.query(
                """
                WITH RECURSIVE MonthSeries AS (
                    SELECT DATE_SUB(CURRENT_DATE, INTERVAL 11 MONTH) as date
                    UNION ALL
                    SELECT DATE_ADD(date, INTERVAL 1 MONTH)
                    FROM MonthSeries
                    WHERE date < CURRENT_DATE
                ),
                ServiceData AS (
                    SELECT 
                        s.id,
                        s.name,
                        DATE_FORMAT(sub.start_date, '%Y%m') as month,
                        COUNT(DISTINCT sub.id) as subscriptions,
                        SUM(p.amount) as revenue
                    FROM services s
                    LEFT JOIN subscriptions sub ON s.id = sub.service_id
                    LEFT JOIN payments p ON sub.user_id = p.user_id 
                        AND DATE_FORMAT(p.payment_date, '%Y%m') = DATE_FORMAT(sub.start_date, '%Y%m')
                    WHERE sub.status = 'ACTIVE'
                    GROUP BY s.id, s.name, DATE_FORMAT(sub.start_date, '%Y%m')
                )
                SELECT 
                    s.name,
                    DATE_FORMAT(ms.date, '%M %Y') as month,
                    COALESCE(sd.subscriptions, 0) as subscriptions,
                    COALESCE(sd.revenue, 0) as revenue
                FROM MonthSeries ms
                CROSS JOIN services s
                LEFT JOIN ServiceData sd ON 
                    s.name = sd.name AND 
                    DATE_FORMAT(ms.date, '%Y%m') = sd.month
                ORDER BY s.name, ms.date
                """,
                (rs, rowNum) -> new ServiceHistoricalMetric(
                        rs.getString("name"),
                        rs.getString("month"),
                        rs.getInt("subscriptions"),
                        rs.getDouble("revenue")
                )
        );

        // Get top performing months
        List<TopPerformingMonth> topMonths = jdbcTemplate.query(
                """
                WITH ActiveSubscribers AS (
                    SELECT
                        DATE_FORMAT(start_date, '%Y%m') AS month_key,
                        COUNT(DISTINCT user_id) AS active_subscribers
                    FROM subscriptions
                    WHERE status = 'ACTIVE'
                    GROUP BY DATE_FORMAT(start_date, '%Y%m')
                )
                SELECT
                    DATE_FORMAT(p.payment_date, '%M %Y') AS month,
                    COUNT(DISTINCT s.id) AS new_subscriptions,
                    SUM(p.amount) AS revenue,
                    COALESCE(SUM(asub.active_subscribers), 0) AS active_subscribers
                FROM payments p
                         JOIN subscriptions s
                              ON p.user_id = s.user_id
                                  AND DATE_FORMAT(p.payment_date, '%Y%m') = DATE_FORMAT(s.start_date, '%Y%m')
                         LEFT JOIN ActiveSubscribers asub
                                   ON DATE_FORMAT(p.payment_date, '%Y%m') = asub.month_key
                WHERE p.status = 'PAID'
                GROUP BY DATE_FORMAT(p.payment_date, '%M %Y')
                ORDER BY revenue DESC
                LIMIT 5;
                """,
                (rs, rowNum) -> new TopPerformingMonth(
                        rs.getString("month"),
                        rs.getInt("new_subscriptions"),
                        rs.getDouble("revenue"),
                        rs.getInt("active_subscribers")
                )
        );

        return new HistoricalMetrics(yearlyMetrics, serviceMetrics, topMonths);
    }
}

@Data
@AllArgsConstructor
class HistoricalMetrics {
    private List<YearlyMetric> yearlyMetrics;
    private List<ServiceHistoricalMetric> serviceMetrics;
    private List<TopPerformingMonth> topMonths;
}

@Data
@AllArgsConstructor
class YearlyMetric {
    private Integer year;
    private Double revenue;
    private Double expenses;
    private Double netProfit;
}

@Data
@AllArgsConstructor
class ServiceHistoricalMetric {
    private String serviceName;
    private String month;
    private Integer subscriptions;
    private Double revenue;
}

@Data
@AllArgsConstructor
class TopPerformingMonth {
    private String month;
    private Integer newSubscriptions;
    private Double revenue;
    private Integer activeSubscribers;
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
    private Double monthlyExpenses;
    private Double netProfit;
    private Double profitMargin;
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

@Data
@AllArgsConstructor
class ExpenseTrend {
    private String month;
    private Double totalExpenses;
    private Double recurringExpenses;
    private Double oneTimeExpenses;
}

@Data
@AllArgsConstructor
class ExpenseByCategory {
    private String category;
    private Double amount;
    private Integer transactionCount;
}

@Data
@AllArgsConstructor
class ProfitabilityMetrics {
    private Double revenue;
    private Double expenses;
    private Double netProfit;
    private Double profitMargin;
}