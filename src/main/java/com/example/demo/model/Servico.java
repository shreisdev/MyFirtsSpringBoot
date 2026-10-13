package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Servico oferecido pela barbearia (corte, barba, etc).
 *
 * Observacao: no diagrama o atributo {@code preco} esta como "double".
 * Aqui usamos {@link BigDecimal} porque e o tipo correto para dinheiro
 * (evita erros de arredondamento do ponto flutuante).
 */
@Entity
@Table(name = "servico")
@Getter
@Setter
@NoArgsConstructor
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(name = "duracao_minutos", nullable = false)
    private int duracaoMinutos;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false)
    private boolean ativo = true;

    /** Inativa o servico para que ele nao apareca mais no agendamento. */
    public void inativar() {
        this.ativo = false;
    }

    /** Reativa o servico. */
    public void ativar() {
        this.ativo = true;
    }

    /**
     * Alias usado pelo front-end (templates usam {@code s.duracaoMin}).
     * Mantem o atributo {@code duracaoMinutos} do diagrama e evita quebrar a view.
     */
    public int getDuracaoMin() {
        return duracaoMinutos;
    }
}
