package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Fidelidade do cliente.
 *
 * RF20: nao existe a classe Cliente (nao ha login/cadastro - RF05).
 * Por isso o e-mail informado no formulario e o unico identificador
 * possivel para acumular fidelidade, sendo usado como chave primaria.
 */
@Entity
@Table(name = "registro_fidelidade")
@Getter
@Setter
@NoArgsConstructor
public class RegistroFidelidade {

    /** A cada N atendimentos concluidos o cliente fica elegivel a uma recompensa. */
    public static final int META_ATENDIMENTOS = 10;

    @Id
    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "quantidade_atendimentos", nullable = false)
    private int quantidadeAtendimentos;

    @Column(name = "ultima_atualizacao")
    private LocalDateTime ultimaAtualizacao = LocalDateTime.now();

    /** Conta mais um atendimento concluido para este e-mail. */
    public void incrementar() {
        this.quantidadeAtendimentos++;
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    /** O cliente ja atingiu a meta de atendimentos? */
    public boolean elegivelParaRecompensa() {
        return this.quantidadeAtendimentos >= META_ATENDIMENTOS;
    }
}
