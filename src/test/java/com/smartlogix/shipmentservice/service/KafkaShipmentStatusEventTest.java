package com.smartlogix.shipmentservice.service;


import com.smartlogix.shipmentservice.dto.UpdateShipmentStatusRequest;
import com.smartlogix.shipmentservice.entity.Shipment;
import com.smartlogix.shipmentservice.repository.ShipmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.DirtiesContext;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**this test verifies that ShipmentService correctly PUBLISHES to the "shipment-status-changed" topic.
 * (Mirror of KafkaOrderCreatedEventTest in OrderService, same pattern)
 * Flow being tested:
 *  Admin changes shipment status (ie: "en_transito")
 *    ShipmentService updates DB
 *      ShipmentService.notifyOrderService() publishes Kafka event
 *          TestKafkaConsumer "spy" captures it
 *             we verify the event JSON contains the right data
 */

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = {"shipment-status-changed", "order-created"},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)

@DirtiesContext
@Import(KafkaShipmentStatusEventTest.TestKafkaConsumer.class)
public class KafkaShipmentStatusEventTest {

    @Autowired
    ShipmentService shipmentService;

    @Autowired
    ShipmentRepository shipmentRepository;

    @Autowired
    TestKafkaConsumer testConsumer;

    @Test
    void whenShipmentStatusChangedToEnTransito_eventPublishedToKafka() throws InterruptedException {

        //1- Create a real shipment in DB
        Shipment shipment = new Shipment();
        shipment.setOrderId(42L);
        shipment.setShipmentStatus("pendiente");
        shipment.setShippingCompany("ChileExpress");
        shipment.setShippingAddress("Av. Providencia 1234");
        Shipment saved = shipmentRepository.save(shipment);

        System.out.println("Created shipment #" + saved.getShipmentId() + " for order #42");

        //2- Change status to en_transito (this should trigger Kafka event)
        UpdateShipmentStatusRequest request = new UpdateShipmentStatusRequest();
        request.setNewStatus("en_transito");

        shipmentService.updateShipmentStatus(saved.getShipmentId(), request);

        //3- Wait for event to arrive
        String receivedMessage = testConsumer.messages.poll(5, TimeUnit.SECONDS);

        //4- verify
        assertNotNull(receivedMessage,
                "No message received from shipment-status-changed topic");

        System.out.println("Received event: " + receivedMessage);

        assertTrue(receivedMessage.contains("\"orderId\":42"),
                "Event should contain the orderId");
        assertTrue(receivedMessage.contains("\"newOrderStatus\":\"enviada\""),
                "en_transito shipment should map to enviada order status");


        System.out.println("TEST PASSED: shipment-status-changed EVENT PUBLISHED CORRECTLY !");
        System.out.println("Event: " + receivedMessage);

    }

    @Test
    void whenShipmentStatusChangedToEntregado_eventPublishedWithCompletada()
            throws InterruptedException {

        //Create another shipment
        Shipment shipment = new Shipment();
        shipment.setOrderId(43L);
        shipment.setShipmentStatus("en_transito");
        shipment.setShippingCompany("DHL");
        shipment.setShippingAddress("Calle Test 123");
        Shipment saved = shipmentRepository.save(shipment);

        UpdateShipmentStatusRequest request = new UpdateShipmentStatusRequest();
        request.setNewStatus("entregado");

        shipmentService.updateShipmentStatus(saved.getShipmentId(), request);

        String receivedMessage = testConsumer.messages.poll(5, TimeUnit.SECONDS);

        assertNotNull(receivedMessage, "No message received");
        assertTrue(receivedMessage.contains("\"newOrderStatus\":\"completada\""),
                "entregado shipment should map to completada order status");


        System.out.println("TEST PASSED: entregado --> completada EVENT PUBLISHED CORRECTLY !");
        System.out.println("Event: " + receivedMessage);

    }

    //Spy consumer (captures messages from the topic for inspection)
    @Component
    static class TestKafkaConsumer {
        BlockingQueue<String> messages = new LinkedBlockingQueue<>();

        @KafkaListener(
                topics = "shipment-status-changed",
                groupId = "test-shipment-consumer-group"
        )
        public void consume(String message) {
            System.out.println("[TestConsumer] Captured: " + message);
            messages.add(message);
        }
    }
}
