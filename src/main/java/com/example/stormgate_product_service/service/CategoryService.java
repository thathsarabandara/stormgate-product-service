package com.example.stormgate_product_service.service;

import com.example.stormgate_product_service.domain.Category;
import com.example.stormgate_product_service.dto.CategoryDTO;
import com.example.stormgate_product_service.exception.CategoryNotFoundException;
import com.example.stormgate_product_service.mapper.CategoryMapper;
import com.example.stormgate_product_service.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional
    public CategoryDTO createCategory(UUID tenantId, CategoryDTO request) {
        Category category = Category.builder()
                .tenantId(tenantId)
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Category savedCategory = categoryRepository.save(category);
        log.info("Category created: id={}, tenantId={}, name={}", savedCategory.getId(), tenantId, savedCategory.getName());

        return categoryMapper.toDTO(savedCategory);
    }

    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(UUID tenantId, UUID categoryId) {
        Category category = categoryRepository.findByIdAndTenantId(categoryId, tenantId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + categoryId));
        return categoryMapper.toDTO(category);
    }

    @Transactional(readOnly = true)
    public Page<CategoryDTO> listCategories(UUID tenantId, Pageable pageable) {
        Page<Category> categories = categoryRepository.findByTenantId(tenantId, pageable);
        return categories.map(categoryMapper::toDTO);
    }

    @Transactional
    public CategoryDTO updateCategory(UUID tenantId, UUID categoryId, CategoryDTO request) {
        Category category = categoryRepository.findByIdAndTenantId(categoryId, tenantId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + categoryId));

        if (request.getName() != null) {
            category.setName(request.getName());
        }
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        Category updatedCategory = categoryRepository.save(category);
        log.info("Category updated: id={}, tenantId={}", categoryId, tenantId);

        return categoryMapper.toDTO(updatedCategory);
    }

    @Transactional
    public void deleteCategory(UUID tenantId, UUID categoryId) {
        Category category = categoryRepository.findByIdAndTenantId(categoryId, tenantId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + categoryId));

        categoryRepository.delete(category);
        log.info("Category deleted: id={}, tenantId={}", categoryId, tenantId);
    }
}
