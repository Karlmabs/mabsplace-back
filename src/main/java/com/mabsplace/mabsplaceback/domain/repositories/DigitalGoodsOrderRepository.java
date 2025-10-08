package com.mabsplace.mabsplaceback.domain.repositories;

import com.mabsplace.mabsplaceback.domain.entities.DigitalGoodsOrder;
import com.mabsplace.mabsplaceback.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface DigitalGoodsOrderRepository extends JpaRepository<DigitalGoodsOrder, Long> {
    List<DigitalGoodsOrder> findByUser(User user);
    List<DigitalGoodsOrder> findByOrderStatus(DigitalGoodsOrder.OrderStatus orderStatus);

    @Query("SELECT SUM(o.profit) FROM DigitalGoodsOrder o WHERE o.orderStatus = 'DELIVERED'")
    BigDecimal calculateTotalProfit();
}
