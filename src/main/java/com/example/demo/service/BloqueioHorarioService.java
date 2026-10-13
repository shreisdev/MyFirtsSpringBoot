package com.example.demo.service;

import com.example.demo.exception.RecursoNaoEncontradoException;
import com.example.demo.model.BloqueioHorario;
import com.example.demo.model.TipoBloqueio;
import com.example.demo.repository.BloqueioHorarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Regras de negocio relacionadas ao {@link BloqueioHorario}.
 */
@Service
@RequiredArgsConstructor
public class BloqueioHorarioService {

    private final BloqueioHorarioRepository bloqueioHorarioRepository;

    @Transactional(readOnly = true)
    public List<BloqueioHorario> listarPorBarbeiro(Long barbeiroId) {
        return bloqueioHorarioRepository.findByBarbeiroIdOrderByDataHoraInicioAsc(barbeiroId);
    }

    @Transactional(readOnly = true)
    public BloqueioHorario buscarPorIdOuFalhar(Long id) {
        return bloqueioHorarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Bloqueio não encontrado: " + id));
    }

    @Transactional
    public BloqueioHorario salvar(BloqueioHorario bloqueioHorario) {
        LocalDateTime inicio = bloqueioHorario.getDataHoraInicio();
        LocalDateTime fim = bloqueioHorario.getDataHoraFim();
        if (bloqueioHorario.getTipo() == null) {
            throw new IllegalArgumentException("Informe o tipo do bloqueio.");
        }
        if (inicio == null || fim == null || !fim.isAfter(inicio)) {
            throw new IllegalArgumentException("O fim do bloqueio deve ser depois do início.");
        }
        if (bloqueioHorario.getTipo() == TipoBloqueio.RECORRENTE
                && bloqueioHorario.getDiaSemanaRecorrencia() == null) {
            throw new IllegalArgumentException("Bloqueio recorrente exige o dia da semana (1 = segunda ... 7 = domingo).");
        }
        return bloqueioHorarioRepository.save(bloqueioHorario);
    }

    @Transactional
    public void remover(Long id) {
        bloqueioHorarioRepository.deleteById(id);
    }
}
