package com.example.demo.service;

import com.example.demo.exception.RecursoNaoEncontradoException;
import com.example.demo.model.Barbeiro;
import com.example.demo.model.HorarioTrabalho;
import com.example.demo.repository.HorarioTrabalhoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

/**
 * Regras de negocio relacionadas ao {@link HorarioTrabalho}.
 */
@Service
@RequiredArgsConstructor
public class HorarioTrabalhoService {

    private final HorarioTrabalhoRepository horarioTrabalhoRepository;

    @Transactional(readOnly = true)
    public List<HorarioTrabalho> listarPorBarbeiro(Long barbeiroId) {
        return horarioTrabalhoRepository.findByBarbeiroIdOrderByDiaSemanaAsc(barbeiroId);
    }

    @Transactional(readOnly = true)
    public HorarioTrabalho buscarPorIdOuFalhar(Long id) {
        return horarioTrabalhoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Horário de trabalho não encontrado: " + id));
    }

    @Transactional
    public HorarioTrabalho salvar(HorarioTrabalho horarioTrabalho) {
        if (horarioTrabalho.getHoraInicio() == null || horarioTrabalho.getHoraFim() == null) {
            throw new IllegalArgumentException("Informe o horário de início e fim.");
        }
        if (!horarioTrabalho.getHoraFim().isAfter(horarioTrabalho.getHoraInicio())) {
            throw new IllegalArgumentException("O horário final deve ser depois do inicial.");
        }
        return horarioTrabalhoRepository.save(horarioTrabalho);
    }

    @Transactional
    public HorarioTrabalho salvar(Barbeiro barbeiro, int diaSemana, LocalTime horaInicio, LocalTime horaFim,
                                  LocalTime inicioAlmoco, LocalTime fimAlmoco) {
        HorarioTrabalho horario = new HorarioTrabalho();
        horario.setDiaSemana(diaSemana);
        horario.setHoraInicio(horaInicio);
        horario.setHoraFim(horaFim);
        horario.setInicioAlmoco(inicioAlmoco);
        horario.setFimAlmoco(fimAlmoco);
        horario.setBarbeiro(barbeiro);
        return salvar(horario);
    }

    @Transactional
    public void remover(Long id) {
        horarioTrabalhoRepository.deleteById(id);
    }
}
