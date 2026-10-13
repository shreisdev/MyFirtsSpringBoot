package com.example.demo.service;

import com.example.demo.exception.RecursoNaoEncontradoException;
import com.example.demo.exception.RegraNegocioException;
import com.example.demo.model.Agendamento;
import com.example.demo.model.Avaliacao;
import com.example.demo.model.StatusAgendamento;
import com.example.demo.repository.AgendamentoRepository;
import com.example.demo.repository.AvaliacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Regras de negocio relacionadas a {@link Avaliacao}.
 */
@Service
@RequiredArgsConstructor
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final AgendamentoRepository agendamentoRepository;

    /**
     * Registra a avaliacao de um atendimento concluido.
     * Regras: nota entre 1 e 5 e no maximo uma avaliacao por agendamento.
     */
    @Transactional
    public Avaliacao registrar(Long agendamentoId, int nota, String comentario) {
        if (nota < Avaliacao.NOTA_MINIMA || nota > Avaliacao.NOTA_MAXIMA) {
            throw new RegraNegocioException("A nota deve estar entre 1 e 5.");
        }
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento não encontrado: " + agendamentoId));

        if (agendamento.getStatus() != StatusAgendamento.CONCLUIDO) {
            throw new RegraNegocioException("Só é possível avaliar um atendimento concluído.");
        }
        if (avaliacaoRepository.existsByAgendamentoId(agendamentoId)) {
            throw new RegraNegocioException("Este agendamento já foi avaliado.");
        }

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setNota(nota);
        avaliacao.setComentario(comentario);
        avaliacao.setDataAvaliacao(LocalDateTime.now());
        avaliacao.setAgendamento(agendamento);
        return avaliacaoRepository.save(avaliacao);
    }

    @Transactional(readOnly = true)
    public List<Avaliacao> listarTodas() {
        return avaliacaoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Avaliacao> listarPorBarbeiro(Long barbeiroId) {
        return avaliacaoRepository.findByAgendamentoBarbeiroIdOrderByDataAvaliacaoDesc(barbeiroId);
    }

    /** Media das notas recebidas (0 quando ainda nao ha avaliacoes). */
    @Transactional(readOnly = true)
    public double mediaDasNotas() {
        return avaliacaoRepository.findAll().stream()
                .mapToInt(Avaliacao::getNota)
                .average()
                .orElse(0.0);
    }
}
