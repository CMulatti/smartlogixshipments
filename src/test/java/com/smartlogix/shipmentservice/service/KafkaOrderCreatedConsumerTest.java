package com.smartlogix.shipmentservice.service;


import com.smartlogix.shipmentservice.entity.Shipment;
import com.smartlogix.shipmentservice.repository.ShipmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** This test verifies that ShipmentService correctly CONSUMES the "order-created" Kafka topic.
 Flow being tested:
 *   OrderService publishes event to "order-created"
 *      ShipmentService KafkaConsumerService picks it up
 *          shipmentService.createShipment() is called
 *            Shipment is created in DB with status "pendiente"
 * We simulate OrderService by publishing directly with KafkaTemplate in the test.
 */

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = {"order-created", "shipment-status-changed"},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@DirtiesContext
public class KafkaOrderCreatedConsumerTest {

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate; // to simulate OrderService publishing

    @Autowired
    ShipmentRepository shipmentRepository;

    @Test
    void whenOrderCreatedEventReceived_shipmentIsCreated() throws InterruptedException {

        Long testOrderId = 999L; //We use a unique id so we can find it after

        //Simulate OrderService publishing the order-created event
        //this is exactly the JSON that OrderService.createOrder() produces
        String eventJson = String.format(
                "{\"orderId\":%d,\"shippingCompany\":\"DHL\",\"shippingAddress\":\"Av. Providencia 1234\"}",
                testOrderId
        );

        kafkaTemplate.send("order-created", eventJson);
        System.out.println("Published event to order-created: " + eventJson);

        //Wait for ShipmentService's KafkaConsumerService to process it
        TimeUnit.SECONDS.sleep(3);

        // Check that a shipment was created for this order
        Optional<Shipment> shipment = shipmentRepository.findByOrderId(testOrderId);

        assertTrue(shipment.isPresent(),
                "Shipment should have been created after order-created event");
        assertEquals("pendiente", shipment.get().getShipmentStatus(),
                "New shipment should start as pendiente");
        assertEquals("DHL", shipment.get().getShippingCompany());
        assertEquals("Av. Providencia 1234", shipment.get().getShippingAddress());


        System.out.println("TEST PASSED: SHIPMENTSERVICE CORRECTLY CONSUMED order-created EVENT!");
        System.out.println("Shipment #" + shipment.get().getShipmentId()
                + " created for order #" + testOrderId
                + " with status: " + shipment.get().getShipmentStatus());

    }
}
