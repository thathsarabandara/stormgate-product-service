package com.example.stormgate_product_service.service;

import com.example.stormgate_product_service.event.ProductCreatedEvent;
import com.example.stormgate_product_service.event.StockUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.product-created:product-created}")
    private String productCreatedTopic;

    @Value("${kafka.topic.stock-updated:stock-updated}")
    private String stockUpdatedTopic;

    public void publishProductCreated(UUID tenantId, UUID productId, String productName, BigDecimal price) {
        ProductCreatedEvent event = ProductCreatedEvent.builder()
                .eventType("PRODUCT_CREATED")
                .productId(productId)
                .tenantId(tenantId)
                .productName(productName)
                .price(price)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send(productCreatedTopic, productId.toString(), event);
        log.info("Published ProductCreatedEvent for product {} in tenant {}", productId, tenantId);
    }

    public void publishStockUpdated(UUID tenantId, UUID productId, Integer oldStock, Integer newStock, String reason) {
        StockUpdatedEvent event = StockUpdatedEvent.builder()
                .eventType("STOCK_UPDATED")
                .productId(productId)
                .tenantId(tenantId)
                .oldStock(oldStock)
                .newStock(newStock)
                .reason(reason)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send(stockUpdatedTopic, productId.toString(), event);
        log.info("Published StockUpdatedEvent for product {} in tenant {}", productId, tenantId);
    }
}
