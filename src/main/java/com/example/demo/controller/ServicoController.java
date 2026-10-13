package com.example.demo.controller;

import com.example.demo.exception.RecursoNaoEncontradoException;
import com.example.demo.exception.RegraNegocioException;
import com.example.demo.model.Servico;
import com.example.demo.service.ServicoService;
import com.example.demo.util.SessaoAdmin;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

/**
 * CRUD de {@link Servico} no painel administrativo.
 * Telas em {@code templates/admin/servicos/}.
 */
@Controller
@RequestMapping("/admin/servicos")
@RequiredArgsConstructor
public class ServicoController {

    private final ServicoService servicoService;

    @GetMapping
    public String listar(HttpSession session, Model model) {
        if (!SessaoAdmin.estaLogado(session)) {
            return "redirect:/admin/login";
        }
        model.addAttribute("servicos", servicoService.listarTodos());
        return "admin/servicos/lista";
    }

    @GetMapping("/novo")
    public String novo(HttpSession session, Model model) {
        if (!SessaoAdmin.estaLogado(session)) {
            return "redirect:/admin/login";
        }
        model.addAttribute("servico", new Servico());
        model.addAttribute("edicao", false);
        return "admin/servicos/form";
    }

    @PostMapping
    public String criar(@RequestParam("nome") String nome,
                        @RequestParam(name = "duracaoMinutos", defaultValue = "0") int duracaoMinutos,
                        @RequestParam(name = "preco", required = false) BigDecimal preco,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        if (!SessaoAdmin.estaLogado(session)) {
            return "redirect:/admin/login";
        }
        try {
            servicoService.criar(nome, duracaoMinutos, preco);
            redirectAttributes.addFlashAttribute("mensagem", "Serviço cadastrado com sucesso.");
        } catch (RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/servicos";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable("id") Long id, HttpSession session, Model model) {
        if (!SessaoAdmin.estaLogado(session)) {
            return "redirect:/admin/login";
        }
        model.addAttribute("servico", servicoService.buscarPorIdOuFalhar(id));
        model.addAttribute("edicao", true);
        return "admin/servicos/form";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable("id") Long id,
                            @RequestParam("nome") String nome,
                            @RequestParam(name = "duracaoMinutos", defaultValue = "0") int duracaoMinutos,
                            @RequestParam(name = "preco", required = false) BigDecimal preco,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        if (!SessaoAdmin.estaLogado(session)) {
            return "redirect:/admin/login";
        }
        try {
            servicoService.atualizar(id, nome, duracaoMinutos, preco);
            redirectAttributes.addFlashAttribute("mensagem", "Serviço atualizado.");
        } catch (RegraNegocioException | RecursoNaoEncontradoException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/servicos";
    }

    @GetMapping("/{id}/ativar")
    public String ativar(@PathVariable("id") Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!SessaoAdmin.estaLogado(session)) {
            return "redirect:/admin/login";
        }
        try {
            servicoService.ativar(id);
            redirectAttributes.addFlashAttribute("mensagem", "Serviço ativado.");
        } catch (RecursoNaoEncontradoException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/servicos";
    }

    @GetMapping("/{id}/inativar")
    public String inativar(@PathVariable("id") Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!SessaoAdmin.estaLogado(session)) {
            return "redirect:/admin/login";
        }
        try {
            servicoService.inativar(id);
            redirectAttributes.addFlashAttribute("mensagem", "Serviço inativado.");
        } catch (RecursoNaoEncontradoException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/servicos";
    }
}
