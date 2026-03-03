package com.citynet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CityNetApplication {

    // Entry point for the Spring Boot backend.
    // This class wires controllers, services, and algorithms together.
    public static void main(String[] args) {
        SpringApplication.run(CityNetApplication.class, args);
    }
}

