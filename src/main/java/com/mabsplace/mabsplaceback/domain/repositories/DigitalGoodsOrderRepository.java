package com.mabsplace.mabsplaceback.domain.repositories;

import com.mabsplace.mabsplaceback.domain.entities.DigitalGoodsOrder;
import com.mabsplace.mabsplaceback.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public interface DigitalGoodsOrderRepository extends JpaRepository<DigitalGoodsOrder, Long> {
    List<DigitalGoodsOrder> findByUser(User user);
    List<DigitalGoodsOrder> findByOrderStatus(DigitalGoodsOrder.OrderStatus orderStatus);

    @Query("SELECT SUM(o.profit) FROM DigitalGoodsOrder o WHERE o.orderStatus = 'DELIVERED'")
    BigDecimal calculateTotalProfit();

    // Analytics queries
    @Query("SELECT COUNT(o) FROM DigitalGoodsOrder o WHERE o.orderStatus IN ('PAID', 'DELIVERED')")
    Long countCompletedOrders();

    @Query("SELECT COUNT(o) FROM DigitalGoodsOrder o WHERE o.createdAt >= :startDate AND o.orderStatus IN ('PAID', 'DELIVERED')")
    Long countCompletedOrdersSince(@Param("startDate") Date startDate);

    @Query("SELECT SUM(o.totalAmount) FROM DigitalGoodsOrder o WHERE o.orderStatus IN ('PAID', 'DELIVERED')")
    BigDecimal calculateTotalRevenue();

    @Query("SELECT SUM(o.totalAmount) FROM DigitalGoodsOrder o WHERE o.createdAt >= :startDate AND o.orderStatus IN ('PAID', 'DELIVERED')")
    BigDecimal calculateRevenueSince(@Param("startDate") Date startDate);

    @Query("SELECT SUM(o.profit) FROM DigitalGoodsOrder o WHERE o.createdAt >= :startDate AND o.orderStatus = 'DELIVERED'")
    BigDecimal calculateProfitSince(@Param("startDate") Date startDate);

    @Query("SELECT AVG(o.totalAmount) FROM DigitalGoodsOrder o WHERE o.orderStatus IN ('PAID', 'DELIVERED')")
    BigDecimal calculateAverageOrderValue();

    @Query("SELECT COUNT(DISTINCT o.user) FROM DigitalGoodsOrder o WHERE o.orderStatus IN ('PAID', 'DELIVERED')")
    Long countUniqueCustomers();

    // Monthly revenue trend (last 6 months)
    @Query(value = "SELECT DATE_FORMAT(created_at, '%Y-%m') as month, " +
            "SUM(total_amount) as revenue, " +
            "COUNT(*) as orderCount " +
            "FROM digital_goods_orders " +
            "WHERE order_status IN ('PAID', 'DELIVERED') " +
            "AND created_at >= DATE_SUB(NOW(), INTERVAL 6 MONTH) " +
            "GROUP BY DATE_FORMAT(created_at, '%Y-%m') " +
            "ORDER BY month ASC", nativeQuery = true)
    List<Object[]> getMonthlyRevenueTrend();

    // Top products by orders
    @Query("SELECT o.product.id, o.product.name, COUNT(o) as orderCount, SUM(o.totalAmount) as revenue, SUM(o.profit) as profit " +
            "FROM DigitalGoodsOrder o " +
            "WHERE o.orderStatus IN ('PAID', 'DELIVERED') " +
            "GROUP BY o.product.id, o.product.name " +
            "ORDER BY orderCount DESC")
    List<Object[]> getTopProductsByOrders();

    // Top products by revenue
    @Query("SELECT o.product.id, o.product.name, COUNT(o) as orderCount, SUM(o.totalAmount) as revenue, SUM(o.profit) as profit " +
            "FROM DigitalGoodsOrder o " +
            "WHERE o.orderStatus IN ('PAID', 'DELIVERED') " +
            "GROUP BY o.product.id, o.product.name " +
            "ORDER BY revenue DESC")
    List<Object[]> getTopProductsByRevenue();

    // Order status distribution
    @Query("SELECT o.orderStatus, COUNT(o) FROM DigitalGoodsOrder o GROUP BY o.orderStatus")
    List<Object[]> getOrderStatusDistribution();

    // Recent orders (last 30 days)
    @Query("SELECT o FROM DigitalGoodsOrder o WHERE o.createdAt >= :startDate ORDER BY o.createdAt DESC")
    List<DigitalGoodsOrder> findRecentOrders(@Param("startDate") Date startDate);
}
