package com.smartlogix.shipmentservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlogix.shipmentservice.dto.CreateShipmentRequest;
import com.smartlogix.shipmentservice.dto.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final ShipmentService shipmentService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order-created", groupId = "shipment-service-group")
    public void handleOrderCreated(String payload) {
        try {
            System.out.println("[ShipmentService] Evento recibido: " + payload);

            OrderCreatedEvent event = objectMapper.readValue(payload, OrderCreatedEvent.class);

            CreateShipmentRequest request = new CreateShipmentRequest();
            request.setOrderId(event.getOrderId());
            request.setShippingCompany(event.getShippingCompany());
            request.setShippingAddress(event.getShippingAddress());

            shipmentService.createShipment(request);

            System.out.println("[ShipmentService] Envio creado para pedido con orderId: " + event.getOrderId());

        } catch (Exception e) {
            System.err.println("[ShipmentService] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
