//outgoing Kafka message published to the shipment-status-changed topic when the admin changes a shipment status.
// OrderService reads this to update the corresponding order status.

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