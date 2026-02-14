package com.example.stormgate_product_service.service;

import com.example.stormgate_product_service.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventListener {

    private final InventoryService inventoryService;

    @KafkaListener(
            topics = "${kafka.topic.order-created:order-created}",
            groupId = "${spring.kafka.consumer.group-id:product-service}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderCreated(OrderCreatedEvent event) {
        try {
            log.info("Received OrderCreatedEvent: orderId={}, productId={}, tenantId={}, quantity={}",
                    event.getOrderId(), event.getProductId(), event.getTenantId(), event.getQuantity());

            // Reduce stock when order is created
            inventoryService.reduceStock(event.getTenantId(), event.getProductId(), event.getQuantity());

            log.info("Successfully processed order event: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("Error processing OrderCreatedEvent: {}", event.getOrderId(), e);
            // In a real scenario, you might want to handle this with a dead-letter topic or retry logic
        }
    }
}
