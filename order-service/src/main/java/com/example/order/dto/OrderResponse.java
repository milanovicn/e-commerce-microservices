package com.example.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String customerName;
    private String customerEmail;
    private BigDecimal totalAmount;
    private String status;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
}
