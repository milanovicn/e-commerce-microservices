package com.example.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockCheckResponse {
    private Long productId;
    private String productName;
    private boolean available;
    private Integer availableQuantity;
    private String message;
}
