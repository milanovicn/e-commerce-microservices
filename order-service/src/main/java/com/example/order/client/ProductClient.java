package com.example.order.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductClient {
    
    private final WebClient.Builder webClientBuilder;
    
    @Value("${product.service.url}")
    private String productServiceUrl;
    
    public ProductDTO getProduct(Long productId) {
        log.info("Calling Product Service to get product: {}", productId);
        return webClientBuilder.build()
                .get()
                .uri(productServiceUrl + "/api/products/" + productId)
                .retrieve()
                .bodyToMono(ProductDTO.class)
                .block();
    }
    
    public void reduceStock(Long productId, Integer quantity) {
        log.info("Calling Product Service to reduce stock for product: {}", productId);
        webClientBuilder.build()
                .post()
                .uri(productServiceUrl + "/api/products/" + productId + "/reduce-stock?quantity=" + quantity)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductDTO {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;
        private Integer stockQuantity;
    }
}
