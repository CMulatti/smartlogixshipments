package com.smartlogix.shipmentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class ShipmentserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShipmentserviceApplication.class, args);
	}

}
