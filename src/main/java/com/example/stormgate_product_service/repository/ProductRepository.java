package com.example.stormgate_product_service.repository;

import com.example.stormgate_product_service.domain.Product;
import com.example.stormgate_product_service.domain.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    Page<Product> findByTenantIdAndStatus(UUID tenantId, ProductStatus status, Pageable pageable);

    Page<Product> findByTenantId(UUID tenantId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId AND LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Product> searchProductsByName(@Param("tenantId") UUID tenantId, @Param("search") String search, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId AND p.categoryId = :categoryId")
    Page<Product> findByCategoryId(@Param("tenantId") UUID tenantId, @Param("categoryId") UUID categoryId, Pageable pageable);

    Optional<Product> findByIdAndTenantId(UUID id, UUID tenantId);

    long countByTenantIdAndStatus(UUID tenantId, ProductStatus status);

    List<Product> findByTenantIdAndStatusAndStockLessThan(UUID tenantId, ProductStatus status, Integer stock);

    Optional<Product> findBySkuAndTenantId(String sku, UUID tenantId);
}
