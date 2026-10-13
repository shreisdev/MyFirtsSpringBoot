package com.example.demo.controller;

import com.example.demo.exception.RecursoNaoEncontradoException;
import com.example.demo.model.Barbeiro;
import com.example.demo.service.BarbeiroService;
import com.example.demo.service.HorarioTrabalhoService;
import com.example.demo.util.DiasSemana;
import com.example.demo.util.SessaoAdmin;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalTime;
import java.util.List;

/**
 * Cadastro de {@code HorarioTrabalho} (dias/horas em que cada barbeiro atende).
 * Telas em {@code templates/admin/horarios/}.
 */
@Controller
@RequestMapping("/admin/horarios")
@RequiredArgsConstructor
public class HorarioTrabalhoController {

    private final HorarioTrabalhoService horarioTrabalhoService;
    private final BarbeiroService barbeiroService;

    @GetMapping
    public String listar(@RequestParam(name = "barbeiroId", required = false) Long barbeiroId,
                         HttpSession session,
                         Model model) {
        if (!SessaoAdmin.estaLogado(session)) {
            return "redirect:/admin/login";
        }
        Long referencia = (barbeiroId != null) ? barbeiroId : SessaoAdmin.barbeiroId(session);
        model.addAttribute("barbeiros", barbeiroService.listarTodos());
        model.addAttribute("dias", DiasSemana.nomes());
        model.addAttribute("barbeiroId", referencia);
        model.addAttribute("horarios",
                (referencia != null) ? horarioTrabalhoService.listarPorBarbeiro(referencia) : List.of());
        return "admin/horarios/lista";
    }

    @GetMapping("/novo")
    public String novo(HttpSession session, Model model) {
        if (!SessaoAdmin.estaLogado(session)) {
            return "redirect:/admin/login";
        }
        model.addAttribute("barbeiros", barbeiroService.listarTodos());
        return "admin/horarios/form";
    }

    @PostMapping
    public String criar(@RequestParam("barbeiroId") Long barbeiroId,
                        @RequestParam("diaSemana") int diaSemana,
                        @RequestParam("horaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaInicio,
                        @RequestParam("horaFim") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaFim,
                        @RequestParam(name = "inicioAlmoco", required = false)
                        @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime inicioAlmoco,
                        @RequestParam(name = "fimAlmoco", required = false)
                        @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime fimAlmoco,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        if (!SessaoAdmin.estaLogado(session)) {
            return "redirect:/admin/login";
        }
        try {
            Barbeiro barbeiro = barbeiroService.buscarPorIdOuFalhar(barbeiroId);
            horarioTrabalhoService.salvar(barbeiro, diaSemana, horaInicio, horaFim, inicioAlmoco, fimAlmoco);
            redirectAttributes.addFlashAttribute("mensagem", "Horário de trabalho cadastrado.");
        } catch (RecursoNaoEncontradoException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/horarios?barbeiroId=" + barbeiroId;
    }

    @GetMapping("/{id}/remover")
    public String remover(@PathVariable("id") Long id,
                          @RequestParam(name = "barbeiroId", required = false) Long barbeiroId,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        if (!SessaoAdmin.estaLogado(session)) {
            return "redirect:/admin/login";
        }
        try {
            horarioTrabalhoService.remover(id);
            redirectAttributes.addFlashAttribute("mensagem", "Horário removido.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("erro", "Não foi possível remover: " + e.getMessage());
        }
        return "redirect:/admin/horarios" + (barbeiroId != null ? "?barbeiroId=" + barbeiroId : "");
    }
}
