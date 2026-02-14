package com.example.stormgate_product_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCreatedEvent implements Serializable {
    private String eventType;
    private UUID productId;
    private UUID tenantId;
    private String productName;
    private BigDecimal price;
    private LocalDateTime timestamp;
}
