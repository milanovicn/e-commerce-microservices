package com.example.product.service;

import com.example.product.dto.ProductDTO;
import com.example.product.dto.StockCheckRequest;
import com.example.product.dto.StockCheckResponse;
import com.example.product.model.Product;
import com.example.product.repository.ProductRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;  
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;  
import java.util.concurrent.ConcurrentHashMap; 
import java.util.concurrent.atomic.AtomicInteger; 
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductService {
    
    private final ProductRepository productRepository;
    private final MeterRegistry meterRegistry;

    private final Map<Long, AtomicInteger> stockLevels = new ConcurrentHashMap<>();

    // Custom Metrics
    private final Counter productViewCounter;
    private final Counter stockReductionCounter;
    private final Counter stockCheckCounter;
    private final Timer productFetchTimer;
    
    public ProductService(ProductRepository productRepository, MeterRegistry meterRegistry) {
        this.productRepository = productRepository;
        this.meterRegistry = meterRegistry;
        
        // Initialize custom metrics
        this.productViewCounter = Counter.builder("products.viewed")
                .description("Number of times products were viewed")
                .tag("service", "product-service")
                .register(meterRegistry);
        
        this.stockReductionCounter = Counter.builder("products.stock.reduced")
                .description("Number of times stock was reduced")
                .tag("service", "product-service")
                .register(meterRegistry);
        
        this.stockCheckCounter = Counter.builder("products.stock.checked")
                .description("Number of stock check operations")
                .tag("service", "product-service")
                .register(meterRegistry);
        
        this.productFetchTimer = Timer.builder("products.fetch.time")
                .description("Time taken to fetch products from database")
                .tag("service", "product-service")
                .register(meterRegistry);

        initializeStockGauges();
    }
    
    public List<ProductDTO> getAllProducts() {
        log.info("Fetching all products");
        
        // Increment view counter
        productViewCounter.increment();
        
        // Time the database fetch operation
        return productFetchTimer.record(() -> {
            List<ProductDTO> products = productRepository.findAll().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            
            // Track number of products returned as a gauge
            meterRegistry.gauge("products.catalog.size", products.size());
            
            return products;
        });
    }
    
    public ProductDTO getProductById(Long id) {
        log.info("Fetching product with id: {}", id);
        
        // Increment view counter
        productViewCounter.increment();
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    // Track product not found errors
                    meterRegistry.counter("products.not.found", 
                            "product_id", id.toString()).increment();
                    return new RuntimeException("Product not found with id: " + id);
                });
        
        return convertToDTO(product);
    }
    
    public List<ProductDTO> getAvailableProducts() {
        log.info("Fetching available products");
        
        List<ProductDTO> availableProducts = productRepository.findByStockQuantityGreaterThan(0).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // Track available products count
        meterRegistry.gauge("products.available.count", availableProducts.size());
        
        return availableProducts;
    }
    
    @Transactional
    public StockCheckResponse checkStock(StockCheckRequest request) {
        log.info("Checking stock for product: {} with quantity: {}", 
                 request.getProductId(), request.getQuantity());
        
        // Increment stock check counter
        stockCheckCounter.increment();
        
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        boolean available = product.getStockQuantity() >= request.getQuantity();
        String message = available 
                ? "Stock available" 
                : "Insufficient stock. Available: " + product.getStockQuantity();
        
        // Track stock check results
        if (available) {
            meterRegistry.counter("products.stock.check.result", 
                    "result", "sufficient").increment();
        } else {
            meterRegistry.counter("products.stock.check.result", 
                    "result", "insufficient").increment();
        }
        
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
            // Track insufficient stock events
            meterRegistry.counter("products.stock.insufficient", 
                    "product_id", productId.toString(),
                    "product_name", product.getName())
                    .increment();
            throw new RuntimeException("Insufficient stock");
        }
        
        int previousStock = product.getStockQuantity();
        product.setStockQuantity(product.getStockQuantity() - quantity);
        Product savedProduct = productRepository.save(product);
        
        // Increment successful stock reduction counter
        stockReductionCounter.increment();
        
        // Track the quantity of stock reduced
        meterRegistry.counter("products.stock.units.reduced", 
                "product_id", productId.toString())
                .increment(quantity);
        
        // Update the stock level in map
        AtomicInteger stockLevel = stockLevels.computeIfAbsent(productId, 
                id -> {
                    AtomicInteger newLevel = new AtomicInteger(savedProduct.getStockQuantity());
                    meterRegistry.gauge("products.stock.level",
                            Tags.of(
                                    "product_id", productId.toString(),
                                    "product_name", savedProduct.getName()
                            ),
                            newLevel,
                            AtomicInteger::get);
                    return newLevel;
                });
        stockLevel.set(savedProduct.getStockQuantity());
        
        log.info("Stock reduced successfully. Previous: {}, Reduced: {}, New: {}", 
                previousStock, quantity, savedProduct.getStockQuantity());
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

    private void initializeStockGauges() {
        // Load all products and register gauges
        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            AtomicInteger stockLevel = new AtomicInteger(product.getStockQuantity());
            stockLevels.put(product.getId(), stockLevel);
            
            // Register gauge with strong reference
            meterRegistry.gauge("products.stock.level",
                    Tags.of(
                            "product_id", product.getId().toString(),
                            "product_name", product.getName()
                    ),
                    stockLevel,
                    AtomicInteger::get);
        }
    }
    
    
}