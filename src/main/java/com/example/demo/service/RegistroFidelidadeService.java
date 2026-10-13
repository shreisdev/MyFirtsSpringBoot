package com.example.demo.service;

import com.example.demo.model.RegistroFidelidade;
import com.example.demo.repository.RegistroFidelidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Regras de negocio relacionadas ao {@link RegistroFidelidade}.
 *
 * RF20: o e-mail do cliente e o unico identificador possivel para acumular
 * fidelidade, pois nao existe login/cadastro de cliente (RF05).
 */
@Service
@RequiredArgsConstructor
public class RegistroFidelidadeService {

    private final RegistroFidelidadeRepository registroFidelidadeRepository;

    /** Soma mais um atendimento concluido ao e-mail informado, criando o registro se necessario. */
    @Transactional
    public RegistroFidelidade registrarAtendimento(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("E-mail é obrigatório para registrar fidelidade.");
        }
        String chave = email.trim().toLowerCase();
        RegistroFidelidade registro = registroFidelidadeRepository.findById(chave)
                .orElseGet(() -> {
                    RegistroFidelidade novo = new RegistroFidelidade();
                    novo.setEmail(chave);
                    novo.setQuantidadeAtendimentos(0);
                    return novo;
                });
        registro.incrementar();
        return registroFidelidadeRepository.save(registro);
    }

    @Transactional(readOnly = true)
    public Optional<RegistroFidelidade> consultar(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return registroFidelidadeRepository.findById(email.trim().toLowerCase());
    }

    @Transactional(readOnly = true)
    public List<RegistroFidelidade> listarTodos() {
        return registroFidelidadeRepository.findAllByOrderByQuantidadeAtendimentosDesc();
    }
}
