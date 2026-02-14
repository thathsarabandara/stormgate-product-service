package com.example.stormgate_product_service.mapper;

import com.example.stormgate_product_service.domain.Product;
import com.example.stormgate_product_service.dto.ProductDTO;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductDTO toDTO(Product product) {
        if (product == null) {
            return null;
        }
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .currency(product.getCurrency())
                .stock(product.getStock())
                .status(product.getStatus().toString())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .sku(product.getSku())
                .categoryId(product.getCategoryId())
                .build();
    }

    public Product toEntity(ProductDTO dto) {
        if (dto == null) {
            return null;
        }
        return Product.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .currency(dto.getCurrency())
                .stock(dto.getStock())
                .sku(dto.getSku())
                .categoryId(dto.getCategoryId())
                .build();
    }
}
