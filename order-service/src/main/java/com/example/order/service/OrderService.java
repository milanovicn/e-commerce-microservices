package com.example.order.service;

import com.example.order.client.NotificationClient;
import com.example.order.client.ProductClient;
import com.example.order.dto.*;
import com.example.order.model.Order;
import com.example.order.model.OrderItem;
import com.example.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final NotificationClient notificationClient;
    
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for customer: {}", request.getCustomerName());
        
        Order order = new Order();
        order.setCustomerName(request.getCustomerName());
        order.setCustomerEmail(request.getCustomerEmail());
        order.setStatus("PENDING");
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        // Process each item and call Product Service
        for (OrderItemRequest itemRequest : request.getItems()) {
            log.info("Processing item - Product ID: {}, Quantity: {}", 
                     itemRequest.getProductId(), itemRequest.getQuantity());
            
            // Call Product Service to get product details
            ProductClient.ProductDTO product = productClient.getProduct(itemRequest.getProductId());
            
            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
            
            // Create order item
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(product.getPrice());
            
            order.addItem(orderItem);
            
            BigDecimal itemTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
            
            // Call Product Service to reduce stock
            productClient.reduceStock(product.getId(), itemRequest.getQuantity());
        }
        
        order.setTotalAmount(totalAmount);
        order.setStatus("CONFIRMED");
        
        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with ID: {}", savedOrder.getId());
        
        // Call Notification Service asynchronously
        String message = String.format(
                "Order #%d confirmed! Total: $%.2f. Items: %d",
                savedOrder.getId(),
                savedOrder.getTotalAmount(),
                savedOrder.getItems().size()
        );
        
        NotificationClient.NotificationRequest notificationRequest = 
                new NotificationClient.NotificationRequest(
                        savedOrder.getId(),
                        savedOrder.getCustomerEmail(),
                        message,
                        "ORDER_CONFIRMATION"
                );
        
        notificationClient.sendOrderNotification(notificationRequest);
        
        return convertToResponse(savedOrder);
    }
    
    public List<OrderResponse> getAllOrders() {
        log.info("Fetching all orders");
        return orderRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public OrderResponse getOrderById(Long id) {
        log.info("Fetching order with id: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        return convertToResponse(order);
    }
    
    public List<OrderResponse> getOrdersByCustomerEmail(String email) {
        log.info("Fetching orders for customer: {}", email);
        return orderRepository.findByCustomerEmail(email).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    private OrderResponse convertToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .collect(Collectors.toList());
        
        return new OrderResponse(
                order.getId(),
                order.getCustomerName(),
                order.getCustomerEmail(),
                order.getTotalAmount(),
                order.getStatus(),
                itemResponses,
                order.getCreatedAt()
        );
    }
}
