package com.example.stormgate_product_service.service;

import com.example.stormgate_product_service.domain.Product;
import com.example.stormgate_product_service.domain.ProductStatus;
import com.example.stormgate_product_service.exception.InsufficientStockException;
import com.example.stormgate_product_service.exception.ProductNotFoundException;
import com.example.stormgate_product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;
    private final ProductStateMachine stateMachine;
    private final ProductEventPublisher eventPublisher;

    @Transactional
    public void reduceStock(UUID tenantId, UUID productId, Integer quantity) {
        Product product = productRepository.findByIdAndTenantId(productId, tenantId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        if (product.getStock() < quantity) {
            throw new InsufficientStockException(
                    String.format("Insufficient stock. Available: %d, Requested: %d", product.getStock(), quantity)
            );
        }

        Integer oldStock = product.getStock();
        product.setStock(product.getStock() - quantity);

        // Update status to OUT_OF_STOCK if stock becomes 0
        if (product.getStock() == 0 && product.getStatus() == ProductStatus.ACTIVE) {
            stateMachine.validateTransition(product.getStatus(), ProductStatus.OUT_OF_STOCK);
            product.setStatus(ProductStatus.OUT_OF_STOCK);
        }

        productRepository.save(product);
        log.info("Stock reduced for product {} from {} to {}", productId, oldStock, product.getStock());

        // Publish event
        eventPublisher.publishStockUpdated(tenantId, productId, oldStock, product.getStock(), "ORDER_CREATED");
    }

    @Transactional
    public void increaseStock(UUID tenantId, UUID productId, Integer quantity) {
        Product product = productRepository.findByIdAndTenantId(productId, tenantId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        Integer oldStock = product.getStock();
        product.setStock(product.getStock() + quantity);

        // Update status to ACTIVE if it was OUT_OF_STOCK
        if (oldStock == 0 && product.getStatus() == ProductStatus.OUT_OF_STOCK) {
            stateMachine.validateTransition(product.getStatus(), ProductStatus.ACTIVE);
            product.setStatus(ProductStatus.ACTIVE);
        }

        productRepository.save(product);
        log.info("Stock increased for product {} from {} to {}", productId, oldStock, product.getStock());

        // Publish event
        eventPublisher.publishStockUpdated(tenantId, productId, oldStock, product.getStock(), "STOCK_REPLENISHMENT");
    }

    @Transactional
    public void updateStock(UUID tenantId, UUID productId, Integer newStock) {
        Product product = productRepository.findByIdAndTenantId(productId, tenantId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        Integer oldStock = product.getStock();
        product.setStock(newStock);

        // Update status based on stock
        if (newStock == 0 && product.getStatus() == ProductStatus.ACTIVE) {
            stateMachine.validateTransition(product.getStatus(), ProductStatus.OUT_OF_STOCK);
            product.setStatus(ProductStatus.OUT_OF_STOCK);
        } else if (newStock > 0 && product.getStatus() == ProductStatus.OUT_OF_STOCK) {
            stateMachine.validateTransition(product.getStatus(), ProductStatus.ACTIVE);
            product.setStatus(ProductStatus.ACTIVE);
        }

        productRepository.save(product);
        log.info("Stock updated for product {} from {} to {}", productId, oldStock, newStock);

        // Publish event
        eventPublisher.publishStockUpdated(tenantId, productId, oldStock, newStock, "MANUAL_UPDATE");
    }

    public Integer getStock(UUID tenantId, UUID productId) {
        Product product = productRepository.findByIdAndTenantId(productId, tenantId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));
        return product.getStock();
    }

    public List<Product> getLowStockProducts(UUID tenantId, Integer threshold) {
        return productRepository.findByTenantIdAndStatusAndStockLessThan(tenantId, ProductStatus.ACTIVE, threshold);
    }
}
