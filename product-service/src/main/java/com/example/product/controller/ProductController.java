package com.example.product.controller;

import com.example.product.dto.ProductDTO;
import com.example.product.dto.StockCheckRequest;
import com.example.product.dto.StockCheckResponse;
import com.example.product.service.ProductService;
import com.example.product.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Product Management", description = "APIs for managing products")
public class ProductController {
    
    private final ProductService productService;
    private final JwtUtil jwtUtil;
    
    @GetMapping({"", "/"})
    @Operation(summary = "Get all products", description = "Retrieve a list of all products")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieve a specific product by its ID")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }
    
    @GetMapping("/available")
    @Operation(summary = "Get available products", description = "Retrieve products with stock > 0")
    public ResponseEntity<List<ProductDTO>> getAvailableProducts() {
        return ResponseEntity.ok(productService.getAvailableProducts());
    }
    
    @PostMapping("/check-stock")
    @Operation(summary = "Check product stock", description = "Verify if sufficient stock is available")
    public ResponseEntity<StockCheckResponse> checkStock(@RequestBody StockCheckRequest request) {
        return ResponseEntity.ok(productService.checkStock(request));
    }
    
    @PostMapping("/{id}/reduce-stock")
    @Operation(summary = "Reduce product stock", description = "Decrease stock quantity for a product")
    public ResponseEntity<Void> reduceStock(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        productService.reduceStock(id, quantity);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/restock")
    @Operation(summary = "Restock product (Admin only)", description = "Increase stock quantity for a product")
    public ResponseEntity<String> restockProduct(
            @PathVariable Long id,
            @RequestParam Integer quantity,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        // Check if token exists
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Authentication required");
        }
        
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        
        // Validate token
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token");
        }
        
        // Check if user is admin
        String role = jwtUtil.extractRole(token);
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Admin access required");
        }
        
        // Perform restock
        productService.restockProduct(id, quantity);
        
        String username = jwtUtil.extractUsername(token);
        return ResponseEntity.ok(String.format("Product restocked successfully by %s", username));
    }
}