package com.example.demo.controller;

import com.example.demo.model.RegistroFidelidade;
import com.example.demo.service.RegistroFidelidadeService;
import com.example.demo.util.SessaoAdmin;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Consulta de {@code RegistroFidelidade} (RF20) no painel administrativo.
 * Tela em {@code templates/admin/fidelidade/}.
 */
@Controller
@RequestMapping("/admin/fidelidade")
@RequiredArgsConstructor
public class FidelidadeController {

    private final RegistroFidelidadeService registroFidelidadeService;

    @GetMapping
    public String listar(@RequestParam(name = "email", required = false) String email,
                         HttpSession session,
                         Model model) {
        if (!SessaoAdmin.estaLogado(session)) {
            return "redirect:/admin/login";
        }
        model.addAttribute("registros", registroFidelidadeService.listarTodos());
        model.addAttribute("meta", RegistroFidelidade.META_ATENDIMENTOS);

        if (email != null && !email.isBlank()) {
            registroFidelidadeService.consultar(email).ifPresentOrElse(
                    registro -> model.addAttribute("consulta", registro),
                    () -> model.addAttribute("erro", "Nenhum registro de fidelidade para " + email + "."));
        }
        return "admin/fidelidade/lista";
    }
}
