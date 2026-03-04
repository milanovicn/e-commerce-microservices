package com.example.product.controller;

import com.example.product.dto.ProductDTO;
import com.example.product.dto.StockCheckRequest;
import com.example.product.dto.StockCheckResponse;
import com.example.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Product Management", description = "APIs for managing products")
public class ProductController {
    
    private final ProductService productService;
    
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
    public ResponseEntity<String> restockProduct(
            @PathVariable Long id,
            @RequestParam Integer quantity,
            @RequestHeader("Authorization") String token) {
        
        // Validate token and check admin role
        if (!isAdmin(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Admin access required");
        }
        
        productService.restockProduct(id, quantity);
        return ResponseEntity.ok("Product restocked successfully");
    }

    private boolean isAdmin(String token) {
        // Call user-service to validate token and check role
        String jwt = token.replace("Bearer ", "");
        
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.exchange(
                "http://user-service:8084/api/auth/validate",
                HttpMethod.GET,
                new HttpEntity<>(createHeaders(jwt)),
                Map.class
            );
            
            // Extract role from response
            // This is simplified - in production, decode JWT or call user service
            return true; // For now, implement proper validation
        } catch (Exception e) {
            return false;
        }
    }

    private HttpHeaders createHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return headers;
    }
}
