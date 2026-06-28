package com.smartlogix.shipmentservice.service;

import com.smartlogix.shipmentservice.dto.CreateShipmentRequest;
import com.smartlogix.shipmentservice.dto.UpdateShipmentStatusRequest;
import com.smartlogix.shipmentservice.entity.Shipment;
import com.smartlogix.shipmentservice.repository.ShipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ShipmentServiceTest {

    @Mock
    ShipmentRepository shipmentRepository;

    @Mock
    KafkaTemplate<String, String> kafkaTemplate; //fake Kafka (no real broker needed)

    @InjectMocks
    ShipmentService shipmentService;

    private Shipment testShipment;

    @BeforeEach
    void setUp() {
        testShipment = new Shipment();
        testShipment.setShipmentId(1L);
        testShipment.setOrderId(10L);
        testShipment.setShipmentStatus("pendiente");
        testShipment.setShippingCompany("ChileExpress");
        testShipment.setShippingAddress("Av. Providencia 1234");
    }

    //T1: createShipment always starts as "pendiente"
    @Test
    void whenShipmentCreated_statusIsPendiente() {
        when(shipmentRepository.save(any(Shipment.class)))
                .thenAnswer(i -> i.getArgument(0));

        CreateShipmentRequest request = new CreateShipmentRequest();
        request.setOrderId(10L);
        request.setShippingCompany("DHL");
        request.setShippingAddress("Av. Providencia 1234");

        Shipment result = shipmentService.createShipment(request);

        assertEquals("pendiente", result.getShipmentStatus(),
                "New shipment should always start as pendiente");
        assertEquals(10L, result.getOrderId());
        assertEquals("DHL", result.getShippingCompany());

        System.out.println("TEST PASSED: NEW SHIPMENT STATUS STARTS AS 'pendiente'");

    }

    //T2: en_transito triggers Kafka event to OrderService
    @Test
    void whenStatusChangedToEnTransito_kafkaEventPublished() {
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));
        when(shipmentRepository.save(any(Shipment.class)))
                .thenAnswer(i -> i.getArgument(0));

        UpdateShipmentStatusRequest request = new UpdateShipmentStatusRequest();
        request.setNewStatus("en_transito");

        shipmentService.updateShipmentStatus(1L, request);

        //Verify Kafka event was published
        verify(kafkaTemplate).send(eq("shipment-status-changed"), anyString());

        System.out.println("TEST PASSED: en_transito TRIGGERS KAFKA EVENT");

    }

    //T3: pendiente does NOT trigger a Kafka event
    @Test
    void whenStatusChangedToPendiente_noKafkaEventPublished() {
        // pendiente --> no corresponding order status change needed
        testShipment.setShipmentStatus("en_transito"); // start from en_transito
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));
        when(shipmentRepository.save(any(Shipment.class)))
                .thenAnswer(i -> i.getArgument(0));

        UpdateShipmentStatusRequest request = new UpdateShipmentStatusRequest();
        request.setNewStatus("pendiente");

        shipmentService.updateShipmentStatus(1L, request);

        //Verify Kafka was NOT called (pendiente has no corresponding order status)
        verify(kafkaTemplate, never()).send(anyString(), anyString());

        System.out.println("TEST PASSED: pendiente DOES NOT TRIGGER KAFKA EVENT");

    }
}

