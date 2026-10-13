package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Ponto de entrada da aplicacao.
 */
@Controller
public class HomeController {

    /** A pagina inicial leva o cliente direto para o formulario de agendamento. */
    @GetMapping("/")
    public String home() {
        return "redirect:/agendar";
    }
}
