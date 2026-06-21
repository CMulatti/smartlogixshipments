package com.smartlogix.shipmentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentStatusChangedEvent {
    private Long orderId;
    private String newOrderStatus;
}