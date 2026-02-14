package com.example.stormgate_product_service.service;

import com.example.stormgate_product_service.domain.Product;
import com.example.stormgate_product_service.domain.ProductStatus;
import com.example.stormgate_product_service.dto.CreateProductRequest;
import com.example.stormgate_product_service.dto.ProductDTO;
import com.example.stormgate_product_service.dto.UpdateProductRequest;
import com.example.stormgate_product_service.exception.ProductNotFoundException;
import com.example.stormgate_product_service.mapper.ProductMapper;
import com.example.stormgate_product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductStateMachine stateMachine;
    private final ProductEventPublisher eventPublisher;

    @Transactional
    public ProductDTO createProduct(UUID tenantId, CreateProductRequest request) {
        Product product = Product.builder()
                .tenantId(tenantId)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .currency(request.getCurrency())
                .stock(request.getStock())
                .sku(request.getSku())
                .categoryId(request.getCategoryId())
                .status(ProductStatus.DRAFT)
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("Product created: id={}, tenantId={}, name={}", savedProduct.getId(), tenantId, savedProduct.getName());

        // Publish product created event
        eventPublisher.publishProductCreated(tenantId, savedProduct.getId(), savedProduct.getName(), savedProduct.getPrice());

        return productMapper.toDTO(savedProduct);
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductById(UUID tenantId, UUID productId) {
        Product product = productRepository.findByIdAndTenantId(productId, tenantId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));
        return productMapper.toDTO(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> listProducts(UUID tenantId, Pageable pageable) {
        Page<Product> products = productRepository.findByTenantId(tenantId, pageable);
        return products.map(productMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> searchProducts(UUID tenantId, String search, Pageable pageable) {
        Page<Product> products = productRepository.searchProductsByName(tenantId, search, pageable);
        return products.map(productMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsByCategory(UUID tenantId, UUID categoryId, Pageable pageable) {
        Page<Product> products = productRepository.findByCategoryId(tenantId, categoryId, pageable);
        return products.map(productMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsByStatus(UUID tenantId, ProductStatus status, Pageable pageable) {
        Page<Product> products = productRepository.findByTenantIdAndStatus(tenantId, status, pageable);
        return products.map(productMapper::toDTO);
    }

    @Transactional
    public ProductDTO updateProduct(UUID tenantId, UUID productId, UpdateProductRequest request) {
        Product product = productRepository.findByIdAndTenantId(productId, tenantId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getStock() != null) {
            product.setStock(request.getStock());
        }
        if (request.getCategoryId() != null) {
            product.setCategoryId(request.getCategoryId());
        }
        if (request.getStatus() != null) {
            ProductStatus newStatus = ProductStatus.valueOf(request.getStatus());
            stateMachine.validateTransition(product.getStatus(), newStatus);
            product.setStatus(newStatus);
        }

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated: id={}, tenantId={}", productId, tenantId);

        return productMapper.toDTO(updatedProduct);
    }

    @Transactional
    public void deleteProduct(UUID tenantId, UUID productId) {
        Product product = productRepository.findByIdAndTenantId(productId, tenantId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));

        // Archive instead of delete for audit trail
        stateMachine.validateTransition(product.getStatus(), ProductStatus.ARCHIVED);
        product.setStatus(ProductStatus.ARCHIVED);
        productRepository.save(product);

        log.info("Product archived: id={}, tenantId={}", productId, tenantId);
    }

    @Transactional(readOnly = true)
    public long countProductsByStatus(UUID tenantId, ProductStatus status) {
        return productRepository.countByTenantIdAndStatus(tenantId, status);
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getLowStockProducts(UUID tenantId, Integer threshold) {
        List<Product> products = productRepository.findByTenantIdAndStatusAndStockLessThan(tenantId, ProductStatus.ACTIVE, threshold);
        return products.stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
    }
}
