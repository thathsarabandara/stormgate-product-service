package com.example.stormgate_product_service.repository;

import com.example.stormgate_product_service.domain.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Page<Category> findByTenantId(UUID tenantId, Pageable pageable);

    Optional<Category> findByIdAndTenantId(UUID id, UUID tenantId);
}
