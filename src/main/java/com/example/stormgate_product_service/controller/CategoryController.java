package com.example.stormgate_product_service.controller;

import com.example.stormgate_product_service.dto.CategoryDTO;
import com.example.stormgate_product_service.service.CategoryService;
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
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management endpoints")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(summary = "Create a new category")
    public ResponseEntity<CategoryDTO> createCategory(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @RequestBody CategoryDTO request) {
        log.info("Creating category for tenant: {}", tenantId);
        CategoryDTO category = categoryService.createCategory(tenantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    @GetMapping("/{categoryId}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<CategoryDTO> getCategory(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable UUID categoryId) {
        log.info("Fetching category: {} for tenant: {}", categoryId, tenantId);
        CategoryDTO category = categoryService.getCategoryById(tenantId, categoryId);
        return ResponseEntity.ok(category);
    }

    @GetMapping
    @Operation(summary = "List all categories")
    public ResponseEntity<Page<CategoryDTO>> listCategories(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            Pageable pageable) {
        log.info("Listing categories for tenant: {}", tenantId);
        Page<CategoryDTO> categories = categoryService.listCategories(tenantId, pageable);
        return ResponseEntity.ok(categories);
    }

    @PutMapping("/{categoryId}")
    @Operation(summary = "Update category")
    public ResponseEntity<CategoryDTO> updateCategory(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable UUID categoryId,
            @RequestBody CategoryDTO request) {
        log.info("Updating category: {} for tenant: {}", categoryId, tenantId);
        CategoryDTO category = categoryService.updateCategory(tenantId, categoryId, request);
        return ResponseEntity.ok(category);
    }

    @DeleteMapping("/{categoryId}")
    @Operation(summary = "Delete category")
    public ResponseEntity<Void> deleteCategory(
            @RequestHeader("X-Tenant-Id") UUID tenantId,
            @PathVariable UUID categoryId) {
        log.info("Deleting category: {} for tenant: {}", categoryId, tenantId);
        categoryService.deleteCategory(tenantId, categoryId);
        return ResponseEntity.noContent().build();
    }
}
