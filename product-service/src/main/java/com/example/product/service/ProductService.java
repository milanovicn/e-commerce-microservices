package com.example.product.service;

import com.example.product.dto.ProductDTO;
import com.example.product.dto.StockCheckRequest;
import com.example.product.dto.StockCheckResponse;
import com.example.product.model.Product;
import com.example.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    
    private final ProductRepository productRepository;
    
    public List<ProductDTO> getAllProducts() {
        log.info("Fetching all products");
        return productRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public ProductDTO getProductById(Long id) {
        log.info("Fetching product with id: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return convertToDTO(product);
    }
    
    public List<ProductDTO> getAvailableProducts() {
        log.info("Fetching available products");
        return productRepository.findByStockQuantityGreaterThan(0).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public StockCheckResponse checkStock(StockCheckRequest request) {
        log.info("Checking stock for product: {} with quantity: {}", 
                 request.getProductId(), request.getQuantity());
        
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        boolean available = product.getStockQuantity() >= request.getQuantity();
        String message = available 
                ? "Stock available" 
                : "Insufficient stock. Available: " + product.getStockQuantity();
        
        return new StockCheckResponse(
                product.getId(),
                product.getName(),
                available,
                product.getStockQuantity(),
                message
        );
    }
    
    @Transactional
    public void reduceStock(Long productId, Integer quantity) {
        log.info("Reducing stock for product: {} by quantity: {}", productId, quantity);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock");
        }
        
        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);
        log.info("Stock reduced successfully. New stock: {}", product.getStockQuantity());
    }
    
    private ProductDTO convertToDTO(Product product) {
        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity()
        );
    }
}
