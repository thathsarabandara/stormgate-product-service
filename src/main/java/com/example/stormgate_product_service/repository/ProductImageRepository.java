package com.example.stormgate_product_service.repository;

import com.example.stormgate_product_service.domain.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {
    List<ProductImage> findByProductId(UUID productId);

    List<ProductImage> findByProductIdAndIsPrimary(UUID productId, Boolean isPrimary);
}
