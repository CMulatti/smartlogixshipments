package com.smartlogix.shipmentservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "shipments")
@Getter
@Setter
@NoArgsConstructor
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shipment_id")
    private Long shipmentId;


    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "shipment_status", length = 20)
    private String shipmentStatus = "pendiente";

    @Column(name = "shipping_company", nullable = false, length = 100)
    private String shippingCompany;

    @Column(name = "shipping_address", nullable = false, length = 255)
    private String shippingAddress;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

