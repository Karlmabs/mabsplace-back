package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.repositories.DigitalGoodsOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class DigitalGoodsAnalyticsService {

    private final DigitalGoodsOrderRepository orderRepository;
    private static final Logger logger = LoggerFactory.getLogger(DigitalGoodsAnalyticsService.class);

    public DigitalGoodsAnalyticsService(DigitalGoodsOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Map<String, Object> getOverviewStats() {
        logger.info("Fetching Digital Goods overview stats");

        // Get current month and previous month dates
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date currentMonthStart = cal.getTime();

        cal.add(Calendar.MONTH, -1);
        Date previousMonthStart = cal.getTime();

        // Calculate stats
        Long totalOrders = orderRepository.countCompletedOrders();
        Long currentMonthOrders = orderRepository.countCompletedOrdersSince(currentMonthStart);
        Long previousMonthOrders = orderRepository.countCompletedOrdersSince(previousMonthStart) - currentMonthOrders;

        BigDecimal totalRevenue = orderRepository.calculateTotalRevenue();
        BigDecimal currentMonthRevenue = orderRepository.calculateRevenueSince(currentMonthStart);
        BigDecimal previousMonthRevenue = orderRepository.calculateRevenueSince(previousMonthStart).subtract(currentMonthRevenue);

        BigDecimal totalProfit = orderRepository.calculateTotalProfit();
        BigDecimal currentMonthProfit = orderRepository.calculateProfitSince(currentMonthStart);
        BigDecimal previousMonthProfit = orderRepository.calculateProfitSince(previousMonthStart).subtract(currentMonthProfit);

        BigDecimal averageOrderValue = orderRepository.calculateAverageOrderValue();
        Long uniqueCustomers = orderRepository.countUniqueCustomers();

        // Calculate growth percentages
        double orderGrowth = calculateGrowthPercentage(previousMonthOrders, currentMonthOrders);
        double revenueGrowth = calculateGrowthPercentage(previousMonthRevenue, currentMonthRevenue);
        double profitGrowth = calculateGrowthPercentage(previousMonthProfit, currentMonthProfit);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", totalOrders != null ? totalOrders : 0L);
        stats.put("currentMonthOrders", currentMonthOrders != null ? currentMonthOrders : 0L);
        stats.put("orderGrowth", orderGrowth);

        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        stats.put("currentMonthRevenue", currentMonthRevenue != null ? currentMonthRevenue : BigDecimal.ZERO);
        stats.put("revenueGrowth", revenueGrowth);

        stats.put("totalProfit", totalProfit != null ? totalProfit : BigDecimal.ZERO);
        stats.put("currentMonthProfit", currentMonthProfit != null ? currentMonthProfit : BigDecimal.ZERO);
        stats.put("profitGrowth", profitGrowth);

        stats.put("averageOrderValue", averageOrderValue != null ? averageOrderValue : BigDecimal.ZERO);
        stats.put("uniqueCustomers", uniqueCustomers != null ? uniqueCustomers : 0L);

        logger.info("Overview stats fetched successfully");
        return stats;
    }

    public List<Map<String, Object>> getRevenueTrend() {
        logger.info("Fetching Digital Goods revenue trend");
        List<Object[]> rawData = orderRepository.getMonthlyRevenueTrend();
        List<Map<String, Object>> trend = new ArrayList<>();

        for (Object[] row : rawData) {
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", row[0]); // YYYY-MM format
            monthData.put("revenue", row[1] != null ? row[1] : BigDecimal.ZERO);
            monthData.put("orderCount", row[2] != null ? row[2] : 0L);
            trend.add(monthData);
        }

        logger.info("Revenue trend fetched: {} months", trend.size());
        return trend;
    }

    public List<Map<String, Object>> getTopProducts(int limit) {
        logger.info("Fetching top {} products by revenue", limit);
        List<Object[]> rawData = orderRepository.getTopProductsByRevenue();
        List<Map<String, Object>> products = new ArrayList<>();

        int count = 0;
        for (Object[] row : rawData) {
            if (count >= limit) break;

            Map<String, Object> product = new HashMap<>();
            product.put("productId", row[0]);
            product.put("productName", row[1]);
            product.put("orderCount", row[2]);
            product.put("revenue", row[3] != null ? row[3] : BigDecimal.ZERO);
            product.put("profit", row[4] != null ? row[4] : BigDecimal.ZERO);
            products.add(product);
            count++;
        }

        logger.info("Top products fetched: {}", products.size());
        return products;
    }

    public Map<String, Long> getOrderStatusDistribution() {
        logger.info("Fetching order status distribution");
        List<Object[]> rawData = orderRepository.getOrderStatusDistribution();
        Map<String, Long> distribution = new HashMap<>();

        for (Object[] row : rawData) {
            String status = row[0].toString();
            Long count = (Long) row[1];
            distribution.put(status, count);
        }

        logger.info("Status distribution fetched: {} statuses", distribution.size());
        return distribution;
    }

    public Map<String, Object> getProductPerformance() {
        logger.info("Fetching product performance metrics");

        List<Object[]> topByOrders = orderRepository.getTopProductsByOrders();
        List<Object[]> topByRevenue = orderRepository.getTopProductsByRevenue();

        List<Map<String, Object>> performanceList = new ArrayList<>();

        // Process all products
        Set<Long> processedIds = new HashSet<>();

        for (Object[] row : topByRevenue) {
            Long productId = ((Number) row[0]).longValue();
            if (!processedIds.contains(productId)) {
                Map<String, Object> perf = new HashMap<>();
                perf.put("productId", productId);
                perf.put("productName", row[1]);
                perf.put("orderCount", row[2]);
                perf.put("revenue", row[3] != null ? row[3] : BigDecimal.ZERO);
                perf.put("profit", row[4] != null ? row[4] : BigDecimal.ZERO);

                // Calculate average order value for this product
                BigDecimal revenue = (BigDecimal) row[3];
                Long orders = ((Number) row[2]).longValue();
                BigDecimal avgOrderValue = BigDecimal.ZERO;
                if (orders > 0 && revenue != null) {
                    avgOrderValue = revenue.divide(BigDecimal.valueOf(orders), 2, RoundingMode.HALF_UP);
                }
                perf.put("avgOrderValue", avgOrderValue);

                performanceList.add(perf);
                processedIds.add(productId);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("products", performanceList);
        result.put("totalProducts", performanceList.size());

        logger.info("Product performance fetched: {} products", performanceList.size());
        return result;
    }

    private double calculateGrowthPercentage(Long previous, Long current) {
        if (previous == null || previous == 0) {
            return current != null && current > 0 ? 100.0 : 0.0;
        }
        if (current == null) {
            return -100.0;
        }
        return ((current - previous) * 100.0) / previous;
    }

    private double calculateGrowthPercentage(BigDecimal previous, BigDecimal current) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current != null && current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        if (current == null) {
            return -100.0;
        }
        BigDecimal growth = current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 2, RoundingMode.HALF_UP);
        return growth.doubleValue();
    }
}
