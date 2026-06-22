//incoming HTTP request from the frontend when admin clicks the status change button.
// Contains just newStatus.
// Used by ShipmentController → ShipmentService.updateShipmentStatus().
//the frontend still calls this directly, it's the entry point for the whole status change flow.

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

