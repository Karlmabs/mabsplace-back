package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.services.DigitalGoodsAnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/digital-goods-analytics")
public class DigitalGoodsAnalyticsController {

    private final DigitalGoodsAnalyticsService analyticsService;
    private static final Logger logger = LoggerFactory.getLogger(DigitalGoodsAnalyticsController.class);

    public DigitalGoodsAnalyticsController(DigitalGoodsAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getOverviewStats() {
        logger.info("Fetching Digital Goods overview stats");
        Map<String, Object> stats = analyticsService.getOverviewStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/revenue-trend")
    public ResponseEntity<List<Map<String, Object>>> getRevenueTrend() {
        logger.info("Fetching Digital Goods revenue trend");
        List<Map<String, Object>> trend = analyticsService.getRevenueTrend();
        return ResponseEntity.ok(trend);
    }

    @GetMapping("/top-products")
    public ResponseEntity<List<Map<String, Object>>> getTopProducts(
            @RequestParam(defaultValue = "5") int limit) {
        logger.info("Fetching top {} products", limit);
        List<Map<String, Object>> products = analyticsService.getTopProducts(limit);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/order-status-distribution")
    public ResponseEntity<Map<String, Long>> getOrderStatusDistribution() {
        logger.info("Fetching order status distribution");
        Map<String, Long> distribution = analyticsService.getOrderStatusDistribution();
        return ResponseEntity.ok(distribution);
    }

    @GetMapping("/product-performance")
    public ResponseEntity<Map<String, Object>> getProductPerformance() {
        logger.info("Fetching product performance metrics");
        Map<String, Object> performance = analyticsService.getProductPerformance();
        return ResponseEntity.ok(performance);
    }
}
