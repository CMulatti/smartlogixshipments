package com.smartlogix.shipmentservice.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**Used when ORDERSERVICE tells SHIPMENTSERVICE to create a new shipment for an order. Example JSON:
 * {
 *   "orderId": 6,
 *   "shippingCompany": "DHL",
 *   "shippingAddress": "Av. Providencia 1234, Santiago"
 * }
 */
@Getter
@Setter
@NoArgsConstructor
public class CreateShipmentRequest {
    private Long orderId;
    private String shippingCompany;
    private String shippingAddress;
}
