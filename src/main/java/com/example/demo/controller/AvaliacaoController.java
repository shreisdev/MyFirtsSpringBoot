package com.example.demo.controller;

import com.example.demo.exception.RecursoNaoEncontradoException;
import com.example.demo.exception.RegraNegocioException;
import com.example.demo.service.AgendamentoService;
import com.example.demo.service.AvaliacaoService;
import com.example.demo.util.SessaoAdmin;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Avaliação do atendimento (1 a 5 estrelas).
 *
 * - Área pública ({@code /avaliar}): o cliente avalia um atendimento concluído.
 * - Painel ({@code /admin/avaliacoes}): o barbeiro vê as avaliações recebidas.
 */
@Controller
@RequiredArgsConstructor
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;
    private final AgendamentoService agendamentoService;

    @GetMapping("/admin/avaliacoes")
    public String listar(HttpSession session, Model model) {
        if (!SessaoAdmin.estaLogado(session)) {
            return "redirect:/admin/login";
        }
        model.addAttribute("avaliacoes", avaliacaoService.listarTodas());
        model.addAttribute("media", avaliacaoService.mediaDasNotas());
        return "admin/avaliacoes/lista";
    }

    /** Formulário público de avaliação, aberto pelo link da tela de status. */
    @GetMapping("/avaliar")
    public String formulario(@RequestParam("agendamentoId") Long agendamentoId, Model model) {
        model.addAttribute("agendamento", agendamentoService.buscarPorIdOuFalhar(agendamentoId));
        return "cliente/avaliar";
    }

    @PostMapping("/avaliar")
    public String registrar(@RequestParam("agendamentoId") Long agendamentoId,
                            @RequestParam("nota") int nota,
                            @RequestParam(name = "comentario", required = false) String comentario,
                            RedirectAttributes redirectAttributes) {
        try {
            avaliacaoService.registrar(agendamentoId, nota, comentario);
            redirectAttributes.addFlashAttribute("mensagem", "Obrigado pela avaliação!");
        } catch (RegraNegocioException | RecursoNaoEncontradoException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }

        String codigo;
        try {
            codigo = agendamentoService.buscarPorIdOuFalhar(agendamentoId).getCodigo();
        } catch (RecursoNaoEncontradoException e) {
            return "redirect:/cliente/status";
        }
        return "redirect:/cliente/status?codigo=" + codigo;
    }
}
