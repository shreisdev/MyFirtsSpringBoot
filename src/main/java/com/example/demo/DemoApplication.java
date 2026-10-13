package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principal da aplicacao (Spring Boot).
 *
 * Projeto: Sistema de agendamento de barbearia.
 * Camadas: model (entidades) -> repository -> service -> controller.
 */
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
