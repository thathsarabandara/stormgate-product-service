package com.example.stormgate_product_service.controller;

import com.example.stormgate_product_service.domain.ProductStatus;
import com.example.stormgate_product_service.dto.CreateProductRequest;
import com.example.stormgate_product_service.dto.ProductDTO;
import com.example.stormgate_product_service.dto.StockUpdateRequest;
import com.example.stormgate_product_service.dto.UpdateProductRequest;
import com.example.stormgate_product_service.service.InventoryService;
import com.example.stormgate_product_service.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product management endpoints")
public class ProductController {

    private final ProductService productService;
    private final InventoryService inventoryService;

    @PostMapping
    @Operation(summary = "Create a new product")
    public ResponseEntity<ProductDTO> createProduct(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @RequestBody CreateProductRequest request) {
        log.info("Creating product for tenant: {}", tenantId);
        ProductDTO product = productService.createProduct(tenantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ProductDTO> getProduct(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable UUID productId) {
        log.info("Fetching product: {} for tenant: {}", productId, tenantId);
        ProductDTO product = productService.getProductById(tenantId, productId);
        return ResponseEntity.ok(product);
    }

    @GetMapping
    @Operation(summary = "List all products with optional filtering")
    public ResponseEntity<Page<ProductDTO>> listProducts(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        log.info("Listing products for tenant: {}", tenantId);
        Page<ProductDTO> products;

        if (search != null && !search.isEmpty()) {
            products = productService.searchProducts(tenantId, search, pageable);
        } else if (status != null && !status.isEmpty()) {
            try {
                ProductStatus productStatus = ProductStatus.valueOf(status.toUpperCase());
                products = productService.getProductsByStatus(tenantId, productStatus, pageable);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        } else {
            products = productService.listProducts(tenantId, pageable);
        }

        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category")
    public ResponseEntity<Page<ProductDTO>> getProductsByCategory(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable UUID categoryId,
            Pageable pageable) {
        log.info("Fetching products for category: {} and tenant: {}", categoryId, tenantId);
        Page<ProductDTO> products = productService.getProductsByCategory(tenantId, categoryId, pageable);
        return ResponseEntity.ok(products);
    }

    @PutMapping("/{productId}")
    @Operation(summary = "Update product")
    public ResponseEntity<ProductDTO> updateProduct(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable UUID productId,
            @RequestBody UpdateProductRequest request) {
        log.info("Updating product: {} for tenant: {}", productId, tenantId);
        ProductDTO product = productService.updateProduct(tenantId, productId, request);
        return ResponseEntity.ok(product);
    }

    @PatchMapping("/{productId}/stock")
    @Operation(summary = "Update product stock")
    public ResponseEntity<Void> updateStock(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable UUID productId,
            @RequestBody StockUpdateRequest request) {
        log.info("Updating stock for product: {} for tenant: {}", productId, tenantId);
        inventoryService.updateStock(tenantId, productId, request.getNewStock());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Delete/Archive product")
    public ResponseEntity<Void> deleteProduct(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable UUID productId) {
        log.info("Deleting product: {} for tenant: {}", productId, tenantId);
        productService.deleteProduct(tenantId, productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get products by status")
    public ResponseEntity<Long> getProductCountByStatus(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable String status) {
        log.info("Counting products with status: {} for tenant: {}", status, tenantId);
        try {
            ProductStatus productStatus = ProductStatus.valueOf(status.toUpperCase());
            long count = productService.countProductsByStatus(tenantId, productStatus);
            return ResponseEntity.ok(count);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock products")
    public ResponseEntity<Object> getLowStockProducts(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @RequestParam(defaultValue = "10") Integer threshold) {
        log.info("Fetching low stock products for tenant: {}", tenantId);
        var products = productService.getLowStockProducts(tenantId, threshold);
        return ResponseEntity.ok(products);
    }
}
