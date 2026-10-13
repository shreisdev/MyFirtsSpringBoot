package com.example.demo.controller;

import com.example.demo.exception.RecursoNaoEncontradoException;
import com.example.demo.exception.RegraNegocioException;
import com.example.demo.service.AgendamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

/**
 * Area do cliente (sem login - RF05): consultar status, cancelar e remarcar.
 * Atende os templates em {@code templates/cliente/}.
 */
@Controller
@RequestMapping("/cliente")
@RequiredArgsConstructor
public class ClienteController {

    private final AgendamentoService agendamentoService;

    /** Consulta o status pelo codigo informado no formulario. */
    @GetMapping("/status")
    public String status(@RequestParam(name = "codigo", required = false) String codigo, Model model) {
        if (codigo != null && !codigo.isBlank()) {
            agendamentoService.consultarPorCodigo(codigo)
                    .ifPresent(agendamento -> model.addAttribute("agendamento", agendamento));
        }
        model.addAttribute("codigo", codigo);
        return "cliente/status";
    }

    /** Link "Ja recebi um link por e-mail" do formulario de agendamento. */
    @GetMapping("/status/consultar")
    public String consultar() {
        return "cliente/status";
    }

    /** Tela de confirmacao de cancelamento. */
    @GetMapping("/cancelar")
    public String telaCancelar(@RequestParam("agendamentoId") Long agendamentoId, Model model) {
        model.addAttribute("agendamentoId", agendamentoId);
        return "cliente/cancelar";
    }

    /** Executa o cancelamento. */
    @PostMapping("/cancelar")
    public String cancelar(@RequestParam("agendamentoId") Long agendamentoId,
                           RedirectAttributes redirectAttributes) {
        try {
            agendamentoService.cancelar(agendamentoId);
            redirectAttributes.addFlashAttribute("mensagem", "Agendamento cancelado com sucesso.");
        } catch (RegraNegocioException | RecursoNaoEncontradoException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/cliente/status";
    }

    /** Tela de remarcacao. */
    @GetMapping("/remarcar")
    public String telaRemarcar(@RequestParam("agendamentoId") Long agendamentoId, Model model) {
        model.addAttribute("agendamentoId", agendamentoId);
        return "cliente/remarcar";
    }

    /** Executa a remarcacao. */
    @PostMapping("/remarcar")
    public String remarcar(@RequestParam("agendamentoId") Long agendamentoId,
                           @RequestParam("novaDataHora")
                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime novaDataHora,
                           RedirectAttributes redirectAttributes) {
        try {
            agendamentoService.remarcar(agendamentoId, novaDataHora);
            redirectAttributes.addFlashAttribute("mensagem", "Agendamento remarcado com sucesso.");
        } catch (RegraNegocioException | RecursoNaoEncontradoException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/cliente/status";
    }
}
