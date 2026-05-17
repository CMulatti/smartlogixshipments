package com.smartlogix.shipmentservice.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**Used when ADMIN clicks status-change button on the frontend/Postman to simulate a status change from an external API. Example JSON:
 * {
 *   "newStatus": "en_transito"
 * }
 */
@Getter
@Setter
@NoArgsConstructor
public class UpdateShipmentStatusRequest {
    private String newStatus; //"en_transito" | "entregado"
}

