package com.smartlogix.shipmentservice.service;

import com.smartlogix.shipmentservice.dto.CreateShipmentRequest;
import com.smartlogix.shipmentservice.dto.UpdateShipmentStatusRequest;
import com.smartlogix.shipmentservice.entity.Shipment;
import com.smartlogix.shipmentservice.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
//    private final RestTemplate restTemplate;
//
//    @Value("${app.orderservice.url}")
//    private String orderServiceUrl;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String SHIPMENT_STATUS_TOPIC = "shipment-status-changed";

    //-----------READ---------------------------

    public List<Shipment> getAllShipments() {
        return shipmentRepository.findAll();
    }

    public Shipment getShipmentById(Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Envio de id " + id + " no encontrado"));
    }

    public Shipment getShipmentByOrderId(Long orderId) {
        return shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("No se encuentra envio para orden: " + orderId));
    }

    //------ CREATE (called by ORDERSERVICE ) --------------
    public Shipment createShipment(CreateShipmentRequest request) {
        Shipment shipment = new Shipment();
        shipment.setOrderId(request.getOrderId());
        shipment.setShippingCompany(request.getShippingCompany());
        shipment.setShippingAddress(request.getShippingAddress());
        shipment.setShipmentStatus("pendiente"); //always starts pending
        return shipmentRepository.save(shipment);
    }

    //--------- UPDATE STATUS --------------------
    /** ADMIN in frontend will simulate the external service for shipments, changing the shipment status with a button (For ex: "en transito")
     * When the change of status happens we:
     * 1. Update shipment_status in OUR database.
     * 2. Figure out what the corresponding order_status should be.
     * 3. Call ORDERSERVICE via HTTP to update that order.
     * Status mapping:
     *      pendiente   → (no change to order, it stays "creada")
     *      en_transito → order becomes "enviada"
     *      entregado   → order becomes "completada"
     * We do NOT loop: ORDERSERVICE's /status endpoint only updates order_status and does NOT call us back.
     */
    public Shipment updateShipmentStatus(Long shipmentId, UpdateShipmentStatusRequest request) {
        Shipment shipment = getShipmentById(shipmentId);

        String newShipmentStatus = request.getNewStatus();
        System.out.println("[ShipmentService] BEFORE save - status: " + shipment.getShipmentStatus());
        shipment.setShipmentStatus(newShipmentStatus);
        System.out.println("[ShipmentService] AFTER set - status: " + shipment.getShipmentStatus());
        Shipment saved = shipmentRepository.save(shipment);
        System.out.println("[ShipmentService] AFTER save - status: " + saved.getShipmentStatus());

        // Determine what order status maps to this shipment status
        String correspondingOrderStatus = mapShipmentStatusToOrderStatus(newShipmentStatus);

        //only notify OrderService if there is a meaningful order status change
        if (correspondingOrderStatus != null) {
            notifyOrderService(shipment.getOrderId(), correspondingOrderStatus);
        }

        return saved;
    }

    //------helpers---
    // returns the order status that corresponds to the shipment status, or null if no update is needed
    private String mapShipmentStatusToOrderStatus(String shipmentStatus) {
        return switch (shipmentStatus) {
            case "en_transito" -> "enviada";
            case "entregado"   -> "completada";
            default -> null; // "pendiente" → no order change needed
        };
    }

//    //Calls ORDERSERVICE VIA REST: PUT orders/status
//    private void notifyOrderService(Long orderId, String newOrderStatus) {
//        String url = orderServiceUrl + "/orders/status";
//
//        Map<String, Object> body = Map.of(
//                "orderId", orderId,
//                "newOrderStatus", newOrderStatus
//        );
//
//        try {
//            restTemplate.put(url, body); //this replaces restTemplate.patchForObject
//        } catch (Exception e) {
//            System.err.println("[ShipmentService] falló en notificar a OrderService: " + e.getMessage());
//        }
//    }

    private void notifyOrderService(Long orderId, String newOrderStatus) {
        String eventJson = String.format(
                "{\"orderId\":%d,\"newOrderStatus\":\"%s\"}",
                orderId,
                newOrderStatus
        );
        kafkaTemplate.send(SHIPMENT_STATUS_TOPIC, eventJson);
        System.out.println("[ShipmentService] Event published to Kafka topic '"
                + SHIPMENT_STATUS_TOPIC + "' for orderId: " + orderId);
    }







    //------------------ DELETE-----------------
    // simulating that for some reason the shipment company could not ship an order already given to them.
    public void deleteShipment(Long id) {
        getShipmentById(id);
        shipmentRepository.deleteById(id);
    }
}
