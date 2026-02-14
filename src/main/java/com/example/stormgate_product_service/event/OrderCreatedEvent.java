package com.example.stormgate_product_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreatedEvent implements Serializable {
    private String eventType;
    private UUID orderId;
    private UUID productId;
    private UUID tenantId;
    private Integer quantity;
    private LocalDateTime timestamp;
}
