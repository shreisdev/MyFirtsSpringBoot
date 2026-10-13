package com.example.demo.controller;

import com.example.demo.dto.AgendamentoForm;
import com.example.demo.exception.RegraNegocioException;
import com.example.demo.model.Agendamento;
import com.example.demo.service.AgendamentoService;
import com.example.demo.service.BarbeiroService;
import com.example.demo.service.ServicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Formulario publico de agendamento (DCU 001).
 * Atende o template {@code templates/agendar/index.html}.
 */
@Controller
@RequiredArgsConstructor
public class AgendamentoController {

    private final AgendamentoService agendamentoService;
    private final BarbeiroService barbeiroService;
    private final ServicoService servicoService;

    /** Exibe o formulario com a lista de barbeiros e servicos ativos. */
    @GetMapping("/agendar")
    public String formulario(Model model) {
        model.addAttribute("barbeiros", barbeiroService.listarTodos());
        model.addAttribute("servicos", servicoService.listarAtivos());
        if (!model.containsAttribute("agendamentoForm")) {
            model.addAttribute("agendamentoForm", new AgendamentoForm());
        }
        return "agendar/index";
    }

    /** Recebe o POST do formulario ({@code POST /agendamentos}). */
    @PostMapping("/agendamentos")
    public String criar(@ModelAttribute("agendamentoForm") AgendamentoForm form,
                        RedirectAttributes redirectAttributes) {
        try {
            Agendamento agendamento = agendamentoService.criar(
                    form.getBarbeiroId(),
                    form.getServicoId(),
                    form.getHorario(),
                    form.getNome(),
                    form.getTelefone(),
                    form.getEmail());

            redirectAttributes.addFlashAttribute("mensagem",
                    "Agendamento confirmado! Seu código é " + agendamento.getCodigo() + ".");
            return "redirect:/cliente/status?codigo=" + agendamento.getCodigo();

        } catch (RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/agendar";
        }
    }
}
