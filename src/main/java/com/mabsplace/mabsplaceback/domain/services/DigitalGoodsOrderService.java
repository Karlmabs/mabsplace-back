package com.mabsplace.mabsplaceback.domain.services;

import com.mabsplace.mabsplaceback.domain.dtos.digitalgoods.DigitalGoodsOrderDto;
import com.mabsplace.mabsplaceback.domain.dtos.digitalgoods.OrderRequestDto;
import com.mabsplace.mabsplaceback.domain.dtos.digitalgoods.PriceCalculationDto;
import com.mabsplace.mabsplaceback.domain.entities.DigitalGoodsOrder;
import com.mabsplace.mabsplaceback.domain.entities.DigitalProduct;
import com.mabsplace.mabsplaceback.domain.entities.Transaction;
import com.mabsplace.mabsplaceback.domain.entities.User;
import com.mabsplace.mabsplaceback.domain.enums.TransactionStatus;
import com.mabsplace.mabsplaceback.domain.enums.TransactionType;
import com.mabsplace.mabsplaceback.domain.mappers.DigitalGoodsOrderMapper;
import com.mabsplace.mabsplaceback.domain.repositories.DigitalGoodsOrderRepository;
import com.mabsplace.mabsplaceback.domain.repositories.DigitalProductRepository;
import com.mabsplace.mabsplaceback.domain.repositories.TransactionRepository;
import com.mabsplace.mabsplaceback.domain.repositories.UserRepository;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class DigitalGoodsOrderService {

    private final DigitalGoodsOrderRepository orderRepository;
    private final DigitalProductRepository productRepository;
    private final UserRepository userRepository;
    private final WalletService walletService;
    private final TransactionRepository transactionRepository;
    private final PriceCalculationService priceCalculationService;
    private final DigitalGoodsOrderMapper orderMapper;
    private static final Logger logger = LoggerFactory.getLogger(DigitalGoodsOrderService.class);

    public DigitalGoodsOrderService(DigitalGoodsOrderRepository orderRepository,
                                     DigitalProductRepository productRepository,
                                     UserRepository userRepository,
                                     WalletService walletService,
                                     TransactionRepository transactionRepository,
                                     PriceCalculationService priceCalculationService,
                                     DigitalGoodsOrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.walletService = walletService;
        this.transactionRepository = transactionRepository;
        this.priceCalculationService = priceCalculationService;
        this.orderMapper = orderMapper;
    }

    public PriceCalculationDto calculateOrderPrice(Long productId, BigDecimal amount) {
        logger.info("Calculating price for product ID: {} with amount: {}", productId, amount);
        DigitalProduct product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("DigitalProduct", "id", productId));

        return priceCalculationService.calculatePrice(product, amount);
    }

    public DigitalGoodsOrderDto createOrder(OrderRequestDto orderRequest) {
        logger.info("Creating digital goods order for user ID: {}", orderRequest.getUserId());

        // Get user and product
        User user = userRepository.findById(orderRequest.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", orderRequest.getUserId()));

        DigitalProduct product = productRepository.findById(orderRequest.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("DigitalProduct", "id", orderRequest.getProductId()));

        // Calculate price
        PriceCalculationDto priceCalc = priceCalculationService.calculatePrice(product, orderRequest.getAmount());

        // Check wallet balance (using correct wallet ID)
        if (!walletService.checkBalance(user.getWallet().getId(), priceCalc.getTotalAmount())) {
            throw new IllegalStateException("Insufficient wallet balance. Required: " + priceCalc.getTotalAmount() + " XAF");
        }

        // Create order
        DigitalGoodsOrder order = DigitalGoodsOrder.builder()
                .user(user)
                .product(product)
                .amount(orderRequest.getAmount())
                .baseCurrency(product.getBaseCurrency())
                .baseCurrencyPrice(priceCalc.getBaseCurrencyPrice())
                .exchangeRate(priceCalc.getExchangeRate())
                .convertedPrice(priceCalc.getConvertedPrice())
                .serviceFee(priceCalc.getServiceFee())
                .totalAmount(priceCalc.getTotalAmount())
                .profit(priceCalc.getServiceFee()) // Profit = service fee for now
                .orderStatus(DigitalGoodsOrder.OrderStatus.PENDING)
                .paymentMethod("WALLET")
                .build();

        DigitalGoodsOrder savedOrder = orderRepository.save(order);
        logger.info("Order created with ID: {}", savedOrder.getId());

        // Create transaction record
        Transaction transaction = Transaction.builder()
                .senderWallet(user.getWallet())
                .receiverWallet(null) // Payment to system
                .amount(priceCalc.getTotalAmount())
                .transactionDate(new Date())
                .currency(null) // Currency stored in order baseCurrency field
                .transactionType(TransactionType.PAYMENT)
                .transactionStatus(TransactionStatus.COMPLETED)
                .transactionRef("DGO-" + savedOrder.getId())
                .build();
        Transaction savedTransaction = transactionRepository.save(transaction);
        logger.info("Transaction created with ID: {} for order ID: {}", savedTransaction.getId(), savedOrder.getId());

        // Debit wallet (using correct wallet ID, not user ID)
        walletService.debit(user.getWallet().getId(), priceCalc.getTotalAmount());
        logger.info("Wallet debited for user ID: {}, wallet ID: {}", user.getId(), user.getWallet().getId());

        // Link transaction to order
        savedOrder.setTransactionId(savedTransaction.getId());

        // Update order status
        savedOrder.setOrderStatus(DigitalGoodsOrder.OrderStatus.PAID);
        savedOrder.setPaidAt(new Date());
        orderRepository.save(savedOrder);

        return orderMapper.toDto(savedOrder);
    }

    public DigitalGoodsOrderDto deliverOrder(Long orderId, String deliveryInfo, String adminNotes, Long adminId) {
        logger.info("Delivering order ID: {} by admin ID: {}", orderId, adminId);

        DigitalGoodsOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("DigitalGoodsOrder", "id", orderId));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminId));

        order.setDeliveryInfo(deliveryInfo);
        order.setAdminNotes(adminNotes);
        order.setDeliveredBy(admin);
        order.setOrderStatus(DigitalGoodsOrder.OrderStatus.DELIVERED);
        order.setDeliveredAt(new Date());

        DigitalGoodsOrder updated = orderRepository.save(order);
        logger.info("Order delivered: {}", orderId);

        return orderMapper.toDto(updated);
    }

    public DigitalGoodsOrderDto updateOrderStatus(Long orderId, DigitalGoodsOrder.OrderStatus newStatus) {
        logger.info("Updating order ID: {} to status: {}", orderId, newStatus);

        DigitalGoodsOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("DigitalGoodsOrder", "id", orderId));

        DigitalGoodsOrder.OrderStatus currentStatus = order.getOrderStatus();

        // Validate status transition
        validateStatusTransition(currentStatus, newStatus);

        // Handle wallet refund for CANCELLED or REFUNDED status
        if ((newStatus == DigitalGoodsOrder.OrderStatus.CANCELLED
            || newStatus == DigitalGoodsOrder.OrderStatus.REFUNDED)
            && (currentStatus == DigitalGoodsOrder.OrderStatus.PAID
                || currentStatus == DigitalGoodsOrder.OrderStatus.PROCESSING
                || currentStatus == DigitalGoodsOrder.OrderStatus.DELIVERED)) {

            // Create refund transaction record
            Transaction refundTransaction = Transaction.builder()
                    .senderWallet(null) // Refund from system
                    .receiverWallet(order.getUser().getWallet())
                    .amount(order.getTotalAmount())
                    .transactionDate(new Date())
                    .currency(null) // Currency stored in order baseCurrency field
                    .transactionType(TransactionType.REFUND)
                    .transactionStatus(TransactionStatus.COMPLETED)
                    .transactionRef("DGO-REFUND-" + order.getId())
                    .build();
            transactionRepository.save(refundTransaction);
            logger.info("Refund transaction created for order ID: {}", order.getId());

            // Refund to wallet (using correct wallet ID, not user ID)
            walletService.credit(order.getUser().getWallet().getId(), order.getTotalAmount());
            logger.info("Refunded {} XAF to user ID: {}, wallet ID: {}",
                order.getTotalAmount(), order.getUser().getId(), order.getUser().getWallet().getId());
        }

        order.setOrderStatus(newStatus);
        DigitalGoodsOrder updated = orderRepository.save(order);

        return orderMapper.toDto(updated);
    }

    private void validateStatusTransition(DigitalGoodsOrder.OrderStatus currentStatus, DigitalGoodsOrder.OrderStatus newStatus) {
        // Define valid transitions
        boolean isValidTransition = switch (currentStatus) {
            case PENDING -> newStatus == DigitalGoodsOrder.OrderStatus.CANCELLED;
            case PAID -> newStatus == DigitalGoodsOrder.OrderStatus.PROCESSING
                    || newStatus == DigitalGoodsOrder.OrderStatus.CANCELLED
                    || newStatus == DigitalGoodsOrder.OrderStatus.REFUNDED;
            case PROCESSING -> newStatus == DigitalGoodsOrder.OrderStatus.DELIVERED
                    || newStatus == DigitalGoodsOrder.OrderStatus.CANCELLED;
            case DELIVERED -> newStatus == DigitalGoodsOrder.OrderStatus.REFUNDED;
            case CANCELLED, REFUNDED -> false; // Terminal states
        };

        if (!isValidTransition) {
            throw new IllegalStateException(
                String.format("Invalid status transition from %s to %s", currentStatus, newStatus)
            );
        }
    }

    public DigitalGoodsOrderDto getOrderById(Long id) {
        logger.info("Fetching order ID: {}", id);
        DigitalGoodsOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DigitalGoodsOrder", "id", id));
        return orderMapper.toDto(order);
    }

    public List<DigitalGoodsOrderDto> getAllOrders() {
        logger.info("Fetching all orders");
        List<DigitalGoodsOrder> orders = orderRepository.findAll();
        logger.info("Found {} orders", orders.size());
        return orderMapper.toDtoList(orders);
    }

    public List<DigitalGoodsOrderDto> getOrdersByUser(Long userId) {
        logger.info("Fetching orders for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        List<DigitalGoodsOrder> orders = orderRepository.findByUser(user);
        logger.info("Found {} orders", orders.size());
        return orderMapper.toDtoList(orders);
    }

    public List<DigitalGoodsOrderDto> getOrdersByStatus(DigitalGoodsOrder.OrderStatus status) {
        logger.info("Fetching orders with status: {}", status);
        List<DigitalGoodsOrder> orders = orderRepository.findByOrderStatus(status);
        logger.info("Found {} orders", orders.size());
        return orderMapper.toDtoList(orders);
    }

    public BigDecimal getTotalProfit() {
        logger.info("Calculating total profit");
        BigDecimal profit = orderRepository.calculateTotalProfit();
        logger.info("Total profit: {}", profit);
        return profit != null ? profit : BigDecimal.ZERO;
    }

    public void deleteOrder(Long orderId) {
        logger.info("Deleting order ID: {}", orderId);

        DigitalGoodsOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("DigitalGoodsOrder", "id", orderId));

        // Check if order can be deleted (only PENDING or CANCELLED orders should be deletable)
        if (order.getOrderStatus() == DigitalGoodsOrder.OrderStatus.PAID
            || order.getOrderStatus() == DigitalGoodsOrder.OrderStatus.PROCESSING
            || order.getOrderStatus() == DigitalGoodsOrder.OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot delete order with status: " + order.getOrderStatus() +
                ". Only PENDING or CANCELLED orders can be deleted.");
        }

        orderRepository.delete(order);
        logger.info("Order deleted: {}", orderId);
    }
}
