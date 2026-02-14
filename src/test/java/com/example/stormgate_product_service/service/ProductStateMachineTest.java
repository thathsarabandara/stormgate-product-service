package com.example.stormgate_product_service.service;

import com.example.stormgate_product_service.domain.Product;
import com.example.stormgate_product_service.domain.ProductStatus;
import com.example.stormgate_product_service.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductStateMachineTest {

    @InjectMocks
    private ProductStateMachine productStateMachine;

    @Test
    void testValidTransitionFromDraftToActive() {
        assertTrue(productStateMachine.isValidTransition(ProductStatus.DRAFT, ProductStatus.ACTIVE));
    }

    @Test
    void testValidTransitionFromDraftToArchived() {
        assertTrue(productStateMachine.isValidTransition(ProductStatus.DRAFT, ProductStatus.ARCHIVED));
    }

    @Test
    void testInvalidTransitionFromArchivedToActive() {
        assertFalse(productStateMachine.isValidTransition(ProductStatus.ARCHIVED, ProductStatus.ACTIVE));
    }

    @Test
    void testValidTransitionFromActiveToOutOfStock() {
        assertTrue(productStateMachine.isValidTransition(ProductStatus.ACTIVE, ProductStatus.OUT_OF_STOCK));
    }

    @Test
    void testValidTransitionFromOutOfStockToActive() {
        assertTrue(productStateMachine.isValidTransition(ProductStatus.OUT_OF_STOCK, ProductStatus.ACTIVE));
    }
}
