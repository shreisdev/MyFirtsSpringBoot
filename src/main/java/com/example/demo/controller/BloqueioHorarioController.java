package com.example.demo.controller;

import com.example.demo.exception.RecursoNaoEncontradoException;
import com.example.demo.model.BloqueioHorario;
import com.example.demo.model.TipoBloqueio;
import com.example.demo.service.BarbeiroService;
import com.example.demo.service.BloqueioHorarioService;
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

import java.time.LocalDateTime;
import java.util.List;

/**
 * Cadastro de {@link BloqueioHorario} (impede agendamentos em certos períodos).
 * Telas em {@code templates/admin/bloqueios/}.
 */
@Controller
@RequestMapping("/admin/bloqueios")
@RequiredArgsConstructor
public class BloqueioHorarioController {

    private final BloqueioHorarioService bloqueioHorarioService;
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
        model.addAttribute("bloqueios",
                (referencia != null) ? bloqueioHorarioService.listarPorBarbeiro(referencia) : List.of());
        return "admin/bloqueios/lista";
    }

    @GetMapping("/novo")
    public String novo(HttpSession session, Model model) {
        if (!SessaoAdmin.estaLogado(session)) {
            return "redirect:/admin/login";
        }
        model.addAttribute("barbeiros", barbeiroService.listarTodos());
        model.addAttribute("tipos", TipoBloqueio.values());
        return "admin/bloqueios/form";
    }

    @PostMapping
    public String criar(@RequestParam("barbeiroId") Long barbeiroId,
                        @RequestParam("tipo") TipoBloqueio tipo,
                        @RequestParam("dataHoraInicio")
                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataHoraInicio,
                        @RequestParam("dataHoraFim")
                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataHoraFim,
                        @RequestParam(name = "diaSemanaRecorrencia", required = false) Integer diaSemanaRecorrencia,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        if (!SessaoAdmin.estaLogado(session)) {
            return "redirect:/admin/login";
        }
        try {
            BloqueioHorario bloqueio = new BloqueioHorario();
            bloqueio.setBarbeiro(barbeiroService.buscarPorIdOuFalhar(barbeiroId));
            bloqueio.setTipo(tipo);
            bloqueio.setDataHoraInicio(dataHoraInicio);
            bloqueio.setDataHoraFim(dataHoraFim);
            if (tipo == TipoBloqueio.RECORRENTE) {
                bloqueio.setDiaSemanaRecorrencia(diaSemanaRecorrencia);
            }
            bloqueioHorarioService.salvar(bloqueio);
            redirectAttributes.addFlashAttribute("mensagem", "Bloqueio cadastrado.");
        } catch (RecursoNaoEncontradoException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/bloqueios?barbeiroId=" + barbeiroId;
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
            bloqueioHorarioService.remover(id);
            redirectAttributes.addFlashAttribute("mensagem", "Bloqueio removido.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("erro", "Não foi possível remover: " + e.getMessage());
        }
        return "redirect:/admin/bloqueios" + (barbeiroId != null ? "?barbeiroId=" + barbeiroId : "");
    }
}
