package com.smartlogix.shipmentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**This DTO mirrors the one in ORDERSERVICE.
 * This is the message we RECEIVE from Kafka.
 * It must match the fields that OrderService puts in its OrderCreatedEvent DTO*/

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {
    private Long orderId;
    private String shippingCompany;
    private String shippingAddress;
}

