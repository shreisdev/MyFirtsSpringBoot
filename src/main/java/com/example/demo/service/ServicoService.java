package com.example.demo.service;

import com.example.demo.exception.RegraNegocioException;
import com.example.demo.exception.RecursoNaoEncontradoException;
import com.example.demo.model.Servico;
import com.example.demo.repository.ServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Regras de negocio relacionadas ao {@link Servico}.
 */
@Service
@RequiredArgsConstructor
public class ServicoService {

    private final ServicoRepository servicoRepository;

    /** Servicos ativos - usados no formulario de agendamento. */
    @Transactional(readOnly = true)
    public List<Servico> listarAtivos() {
        return servicoRepository.findByAtivoTrueOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public List<Servico> listarTodos() {
        return servicoRepository.findAllByOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public Servico buscarPorIdOuFalhar(Long id) {
        return servicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço não encontrado: " + id));
    }

    @Transactional
    public Servico criar(String nome, int duracaoMinutos, BigDecimal preco) {
        validar(nome, duracaoMinutos, preco);
        Servico servico = new Servico();
        servico.setNome(nome);
        servico.setDuracaoMinutos(duracaoMinutos);
        servico.setPreco(preco);
        servico.setAtivo(true);
        return servicoRepository.save(servico);
    }

    @Transactional
    public Servico atualizar(Long id, String nome, int duracaoMinutos, BigDecimal preco) {
        validar(nome, duracaoMinutos, preco);
        Servico servico = buscarPorIdOuFalhar(id);
        servico.setNome(nome);
        servico.setDuracaoMinutos(duracaoMinutos);
        servico.setPreco(preco);
        return servicoRepository.save(servico);
    }

    @Transactional
    public Servico salvar(Servico servico) {
        return servicoRepository.save(servico);
    }

    /** Inativa o servico (nao aparece mais para agendamento). */
    @Transactional
    public Servico inativar(Long id) {
        Servico servico = buscarPorIdOuFalhar(id);
        servico.inativar();
        return servicoRepository.save(servico);
    }

    @Transactional
    public Servico ativar(Long id) {
        Servico servico = buscarPorIdOuFalhar(id);
        servico.ativar();
        return servicoRepository.save(servico);
    }

    private void validar(String nome, int duracaoMinutos, BigDecimal preco) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("Informe o nome do serviço.");
        }
        if (duracaoMinutos <= 0) {
            throw new RegraNegocioException("A duração do serviço deve ser maior que zero.");
        }
        if (preco == null || preco.signum() < 0) {
            throw new RegraNegocioException("Informe um preço válido para o serviço.");
        }
    }
}
