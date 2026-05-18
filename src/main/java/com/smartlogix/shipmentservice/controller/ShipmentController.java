package com.smartlogix.shipmentservice.controller;

import com.smartlogix.shipmentservice.dto.CreateShipmentRequest;
import com.smartlogix.shipmentservice.dto.UpdateShipmentStatusRequest;
import com.smartlogix.shipmentservice.entity.Shipment;
import com.smartlogix.shipmentservice.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @GetMapping
    public ResponseEntity<List<Shipment>> getAllShipments() {
        return ResponseEntity.ok(shipmentService.getAllShipments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Shipment> getShipmentById(@PathVariable Long id) {
        return ResponseEntity.ok(shipmentService.getShipmentById(id));
    }

    //look up a shipment using orderId
    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<Shipment> getShipmentByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(shipmentService.getShipmentByOrderId(orderId));
    }

    //-----------------replaced with Kafka -------------------------------------------------------------
//    // POST /shipments  called by ORDERSERVICE automatically when an order is created
//    @PostMapping
//    public ResponseEntity<Shipment> createShipment(@RequestBody CreateShipmentRequest request) {
//        return ResponseEntity.ok(shipmentService.createShipment(request));
//    }
    //---------------------------------------------------------------------------------------------------
    //Shipments are created internally by the Kafka listener (KafkaConsumerService) when an "order-created" event arrives.


    // "change status" button on admin simulation board on frontend. (from "pendiente" to new status: en_transito  | entregafo)
    // PUT shipments/{id}/status
    // {"newStatus": "en_transito"}
    @PutMapping("/{id}/status")
    public ResponseEntity<Shipment> updateShipmentStatus(@PathVariable Long id, @RequestBody UpdateShipmentStatusRequest request) {
        return ResponseEntity.ok(shipmentService.updateShipmentStatus(id, request));
    }

    //"delete shipment" button on admin simulation board (simulating a shipping company cancelling shipment on a given order)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShipment(@PathVariable Long id) {
        shipmentService.deleteShipment(id);
        return ResponseEntity.noContent().build();
    }
}
