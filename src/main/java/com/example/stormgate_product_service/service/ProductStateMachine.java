package com.example.stormgate_product_service.service;

import com.example.stormgate_product_service.domain.ProductStatus;
import com.example.stormgate_product_service.exception.InvalidProductStatusException;
import org.springframework.stereotype.Component;

@Component
public class ProductStateMachine {

    /**
     * Validates if a transition from currentStatus to newStatus is allowed
     */
    public boolean isValidTransition(ProductStatus currentStatus, ProductStatus newStatus) {
        if (currentStatus == newStatus) {
            return true;
        }

        return switch (currentStatus) {
            case DRAFT -> newStatus == ProductStatus.ACTIVE || newStatus == ProductStatus.ARCHIVED;
            case ACTIVE -> newStatus == ProductStatus.OUT_OF_STOCK || newStatus == ProductStatus.INACTIVE || newStatus == ProductStatus.ARCHIVED;
            case OUT_OF_STOCK -> newStatus == ProductStatus.ACTIVE;
            case INACTIVE -> newStatus == ProductStatus.ACTIVE;
            case ARCHIVED -> false; // Terminal state - no transitions allowed
        };
    }

    /**
     * Validates and performs the status transition
     */
    public void validateTransition(ProductStatus currentStatus, ProductStatus newStatus) {
        if (!isValidTransition(currentStatus, newStatus)) {
            throw new InvalidProductStatusException(
                    String.format("Cannot transition from %s to %s", currentStatus, newStatus)
            );
        }
    }

    /**
     * Returns the next valid statuses for a given status
     */
    public ProductStatus[] getValidNextStatuses(ProductStatus currentStatus) {
        return switch (currentStatus) {
            case DRAFT -> new ProductStatus[]{ProductStatus.ACTIVE, ProductStatus.ARCHIVED};
            case ACTIVE -> new ProductStatus[]{ProductStatus.OUT_OF_STOCK, ProductStatus.INACTIVE, ProductStatus.ARCHIVED};
            case OUT_OF_STOCK -> new ProductStatus[]{ProductStatus.ACTIVE};
            case INACTIVE -> new ProductStatus[]{ProductStatus.ACTIVE};
            case ARCHIVED -> new ProductStatus[]{};
        };
    }
}
