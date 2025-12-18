package com.mabsplace.mabsplaceback.domain.controllers;

import com.mabsplace.mabsplaceback.domain.dtos.digitalgoods.DigitalGoodsOrderDto;
import com.mabsplace.mabsplaceback.domain.dtos.digitalgoods.OrderRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.digitalgoods.PriceCalculationDto;
import com.mabsplace.mabsplaceback.domain.entities.DigitalGoodsOrder;
import com.mabsplace.mabsplaceback.domain.services.DigitalGoodsOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/digital-goods-orders")
public class DigitalGoodsOrderController {

    private final DigitalGoodsOrderService orderService;
    private static final Logger logger = LoggerFactory.getLogger(DigitalGoodsOrderController.class);

    public DigitalGoodsOrderController(DigitalGoodsOrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/calculate-price")
    public ResponseEntity<PriceCalculationDto> calculatePrice(@RequestParam Long productId) {
        logger.info("Calculating price for product ID: {}", productId);
        PriceCalculationDto priceCalc = orderService.calculateOrderPrice(productId);
        return ResponseEntity.ok(priceCalc);
    }

    @PostMapping
    public ResponseEntity<DigitalGoodsOrderDto> createOrder(@RequestBody OrderRequestDto orderRequest) {
        logger.info("Creating digital goods order for user ID: {}", orderRequest.getUserId());
        DigitalGoodsOrderDto created = orderService.createOrder(orderRequest);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PostMapping("/{orderId}/deliver")
    public ResponseEntity<DigitalGoodsOrderDto> deliverOrder(
            @PathVariable Long orderId,
            @RequestBody Map<String, Object> deliveryData) {
        logger.info("Delivering order ID: {}", orderId);
        String deliveryInfo = (String) deliveryData.get("deliveryInfo");
        String adminNotes = (String) deliveryData.get("adminNotes");
        Long adminId = ((Number) deliveryData.get("adminId")).longValue();

        DigitalGoodsOrderDto delivered = orderService.deliverOrder(orderId, deliveryInfo, adminNotes, adminId);
        return ResponseEntity.ok(delivered);
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<DigitalGoodsOrderDto> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam DigitalGoodsOrder.OrderStatus status) {
        logger.info("Updating order ID: {} to status: {}", orderId, status);
        DigitalGoodsOrderDto updated = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DigitalGoodsOrderDto> getOrderById(@PathVariable Long id) {
        logger.info("Fetching order ID: {}", id);
        DigitalGoodsOrderDto order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    public ResponseEntity<List<DigitalGoodsOrderDto>> getAllOrders() {
        logger.info("Fetching all orders");
        List<DigitalGoodsOrderDto> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DigitalGoodsOrderDto>> getOrdersByUser(@PathVariable Long userId) {
        logger.info("Fetching orders for user ID: {}", userId);
        List<DigitalGoodsOrderDto> orders = orderService.getOrdersByUser(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<DigitalGoodsOrderDto>> getOrdersByStatus(@PathVariable DigitalGoodsOrder.OrderStatus status) {
        logger.info("Fetching orders with status: {}", status);
        List<DigitalGoodsOrderDto> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/profit/total")
    public ResponseEntity<Map<String, BigDecimal>> getTotalProfit() {
        logger.info("Fetching total profit");
        BigDecimal profit = orderService.getTotalProfit();
        return ResponseEntity.ok(Map.of("totalProfit", profit));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        logger.info("Deleting order ID: {}", id);
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
