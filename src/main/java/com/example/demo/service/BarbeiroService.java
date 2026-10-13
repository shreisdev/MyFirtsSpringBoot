package com.example.demo.service;

import com.example.demo.exception.RecursoNaoEncontradoException;
import com.example.demo.model.Barbeiro;
import com.example.demo.repository.BarbeiroRepository;
import com.example.demo.util.SenhaUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Regras de negocio relacionadas ao {@link Barbeiro}.
 */
@Service
@RequiredArgsConstructor
public class BarbeiroService {

    private final BarbeiroRepository barbeiroRepository;

    @Transactional(readOnly = true)
    public List<Barbeiro> listarTodos() {
        return barbeiroRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Barbeiro> buscarPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return barbeiroRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Barbeiro buscarPorIdOuFalhar(Long id) {
        return buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Barbeiro não encontrado: " + id));
    }

    @Transactional
    public Barbeiro criar(String nome, String nomeUsuario, String senha, String foto) {
        Barbeiro barbeiro = new Barbeiro();
        barbeiro.setNome(nome);
        barbeiro.setNomeUsuario(nomeUsuario);
        barbeiro.setSenhaHash(SenhaUtil.hash(senha));
        barbeiro.setFoto(foto);
        barbeiro.setTentativasLoginFalhas(0);
        return barbeiroRepository.save(barbeiro);
    }

    @Transactional
    public Barbeiro salvar(Barbeiro barbeiro) {
        return barbeiroRepository.save(barbeiro);
    }

    /**
     * Autentica o barbeiro (RF05) controlando tentativas falhas e bloqueio temporario.
     *
     * @return o barbeiro autenticado, ou {@link Optional#empty()} se as credenciais forem invalidas
     */
    @Transactional
    public Optional<Barbeiro> autenticar(String usuario, String senha) {
        if (usuario == null || senha == null) {
            return Optional.empty();
        }
        return barbeiroRepository.findByNomeUsuario(usuario)
                .map(barbeiro -> {
                    boolean autenticou = barbeiro.autenticar(usuario, senha);
                    // grava o contador de tentativas / bloqueio
                    barbeiroRepository.save(barbeiro);
                    return autenticou ? barbeiro : null;
                });
    }

    /** Troca a senha do barbeiro, guardando apenas o hash. */
    @Transactional
    public void alterarSenha(Long barbeiroId, String novaSenha) {
        Barbeiro barbeiro = buscarPorIdOuFalhar(barbeiroId);
        barbeiro.setSenhaHash(SenhaUtil.hash(novaSenha));
        barbeiro.setTentativasLoginFalhas(0);
        barbeiro.setBloqueadoAte(null);
        barbeiroRepository.save(barbeiro);
    }

    /** O barbeiro esta livre para atender nesse horario? (RF07) */
    @Transactional(readOnly = true)
    public boolean estaDisponivel(Long barbeiroId, LocalDateTime dataHora, int duracaoMinutos) {
        Barbeiro barbeiro = buscarPorIdOuFalhar(barbeiroId);
        return barbeiro.estaDisponivel(dataHora, duracaoMinutos);
    }

    @Transactional
    public void remover(Long barbeiroId) {
        barbeiroRepository.deleteById(barbeiroId);
    }
}
