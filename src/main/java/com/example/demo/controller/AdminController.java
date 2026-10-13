package com.example.demo.controller;

import com.example.demo.exception.RecursoNaoEncontradoException;
import com.example.demo.exception.RegraNegocioException;
import com.example.demo.model.Barbeiro;
import com.example.demo.service.AgendamentoService;
import com.example.demo.service.BarbeiroService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Painel administrativo do barbeiro (RFC/DCU de login e gestao da agenda).
 * Atende os templates em {@code templates/admin/}.
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    /** Chave usada para guardar o barbeiro logado na sessao. */
    public static final String SESSION_BARBEIRO_ID = "barbeiroLogadoId";

    private final BarbeiroService barbeiroService;
    private final AgendamentoService agendamentoService;

    @GetMapping("/login")
    public String telaLogin() {
        return "admin/login";
    }

    /** Autentica o barbeiro (usuario e senha padrao: admin / admin). */
    @PostMapping("/login")
    public String login(@RequestParam("username") String usuario,
                        @RequestParam("password") String senha,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        Optional<Barbeiro> autenticado = barbeiroService.autenticar(usuario, senha);
        if (autenticado.isEmpty()) {
            redirectAttributes.addFlashAttribute("erro", "Usuário ou senha inválidos.");
            return "redirect:/admin/login";
        }
        session.setAttribute(SESSION_BARBEIRO_ID, autenticado.get().getId());
        return "redirect:/admin/painel-agenda";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }

    /** Painel da agenda (o formulario de filtro aponta para /admin/painel). */
    @GetMapping({"/painel-agenda", "/painel"})
    public String painel(@RequestParam(name = "barbeiroId", required = false) Long barbeiroId,
                         @RequestParam(name = "periodo", required = false) String periodo,
                         @RequestParam(name = "data", required = false)
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                         HttpSession session,
                         Model model) {

        if (!estaLogado(session)) {
            return "redirect:/admin/login";
        }

        model.addAttribute("barbeiros", barbeiroService.listarTodos());
        model.addAttribute("agendamentos", agendamentoService.listarParaPainel(barbeiroId, periodo, data));
        model.addAttribute("barbeiroId", barbeiroId);
        model.addAttribute("periodo", periodo);
        model.addAttribute("data", data);
        return "admin/painel-agenda";
    }

    /** Cancela um agendamento pelo painel (link da coluna Acoes). */
    @GetMapping("/cancelar-agendamento")
    public String cancelarAgendamento(@RequestParam("agendamentoId") Long agendamentoId,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        if (!estaLogado(session)) {
            return "redirect:/admin/login";
        }
        try {
            agendamentoService.cancelar(agendamentoId);
            redirectAttributes.addFlashAttribute("mensagem", "Agendamento cancelado.");
        } catch (RegraNegocioException | RecursoNaoEncontradoException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/painel-agenda";
    }

    /**
     * Link "Editar" do painel.
     * Ainda nao existe tela de edicao no front-end, entao apenas avisamos o usuario.
     */
    @GetMapping("/editar-agendamento")
    public String editarAgendamento(@RequestParam("agendamentoId") Long agendamentoId,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        if (!estaLogado(session)) {
            return "redirect:/admin/login";
        }
        redirectAttributes.addFlashAttribute("erro",
                "Tela de edição ainda não implementada no front-end (agendamento " + agendamentoId + ").");
        return "redirect:/admin/painel-agenda";
    }

    /** Marca o atendimento como concluido e acumula fidelidade para o cliente (RF20). */
    @GetMapping("/concluir-agendamento")
    public String concluirAgendamento(@RequestParam("agendamentoId") Long agendamentoId,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        if (!estaLogado(session)) {
            return "redirect:/admin/login";
        }
        try {
            agendamentoService.concluir(agendamentoId);
            redirectAttributes.addFlashAttribute("mensagem",
                    "Atendimento concluído e fidelidade registrada.");
        } catch (RegraNegocioException | RecursoNaoEncontradoException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/painel-agenda";
    }

    /** Registra que o cliente nao compareceu ao horario. */
    @GetMapping("/registrar-falta")
    public String registrarFalta(@RequestParam("agendamentoId") Long agendamentoId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        if (!estaLogado(session)) {
            return "redirect:/admin/login";
        }
        try {
            agendamentoService.registrarFalta(agendamentoId);
            redirectAttributes.addFlashAttribute("mensagem", "Falta registrada.");
        } catch (RegraNegocioException | RecursoNaoEncontradoException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/painel-agenda";
    }

    private boolean estaLogado(HttpSession session) {
        return session != null && session.getAttribute(SESSION_BARBEIRO_ID) != null;
    }
}
