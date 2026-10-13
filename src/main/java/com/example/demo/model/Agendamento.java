package com.example.demo.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Agendamento de um servico com um barbeiro.
 *
 * O cliente NAO possui cadastro/login (RF05 / RF20): ele e identificado
 * pelo e-mail informado no formulario, e pelo {@code tokenAcesso} (codigo)
 * enviado por e-mail para consultar/cancelar/remarcar o agendamento.
 *
 * A restricao de unicidade em (barbeiro_id, data_hora) implementa o RF07:
 * impede dois agendamentos no mesmo horario com o mesmo barbeiro.
 */
@Entity
@Table(
        name = "agendamento",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_agendamento_barbeiro_data_hora",
                columnNames = {"barbeiro_id", "data_hora"}),
        indexes = {
                @Index(name = "idx_agendamento_email", columnList = "email_cliente"),
                @Index(name = "idx_agendamento_token", columnList = "token_acesso")
        })
@Getter
@Setter
@NoArgsConstructor
public class Agendamento {

    /** Prazo minimo (em horas) para cancelar sem virar CANCELADO_TARDIO. */
    public static final long PRAZO_CANCELAMENTO_HORAS = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAgendamento status = StatusAgendamento.CONFIRMADO;

    @Column(name = "nome_cliente", nullable = false)
    private String nomeCliente;

    @Column(name = "telefone_cliente", nullable = false)
    private String telefoneCliente;

    @Column(name = "email_cliente", nullable = false)
    private String emailCliente;

    /** Codigo unico enviado por e-mail; usado pelo cliente para consultar o agendamento. */
    @Column(name = "token_acesso", nullable = false, unique = true)
    private String tokenAcesso;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "barbeiro_id", nullable = false)
    private Barbeiro barbeiro;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;

    @OneToOne(mappedBy = "agendamento", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Avaliacao avaliacao;

    /**
     * Alias usado pelo front-end (templates mostram {@code agendamento.codigo}).
     * O codigo e o proprio {@code tokenAcesso}.
     */
    public String getCodigo() {
        return tokenAcesso;
    }

    /** Duracao (minutos) do servico contratado. */
    public int getDuracaoMinutos() {
        return servico != null ? servico.getDuracaoMinutos() : 0;
    }

    /** Momento em que o atendimento termina. */
    public LocalDateTime getDataHoraFim() {
        return dataHora.plusMinutes(getDuracaoMinutos());
    }

    /** true se o agendamento ainda ocupa a agenda do barbeiro. */
    public boolean estaAtivo() {
        return status == StatusAgendamento.CONFIRMADO;
    }

    public void confirmar() {
        this.status = StatusAgendamento.CONFIRMADO;
    }

    /** Cancela o agendamento, marcando como tardio se estiver fora do prazo. */
    public void cancelar() {
        this.status = estaDentroPrazoCancelamento()
                ? StatusAgendamento.CANCELADO
                : StatusAgendamento.CANCELADO_TARDIO;
    }

    public void concluir() {
        this.status = StatusAgendamento.CONCLUIDO;
    }

    public void registrarFalta() {
        this.status = StatusAgendamento.FALTA;
    }

    /** Move o agendamento para um novo horario e o confirma. */
    public void remarcar(LocalDateTime novoHorario) {
        this.dataHora = novoHorario;
        this.status = StatusAgendamento.CONFIRMADO;
    }

    /** O cancelamento esta dentro do prazo permitido? */
    public boolean estaDentroPrazoCancelamento() {
        return LocalDateTime.now().isBefore(dataHora.minusHours(PRAZO_CANCELAMENTO_HORAS));
    }

    /** true se o intervalo informado se sobrepoe a este agendamento. */
    public boolean conflitaCom(LocalDateTime inicio, int duracaoMinutos) {
        LocalDateTime fim = inicio.plusMinutes(duracaoMinutos);
        return inicio.isBefore(getDataHoraFim()) && fim.isAfter(this.dataHora);
    }

    /** O agendamento ainda pode ser alterado/cancelado? */
    public boolean podeSerAlterado() {
        return status == StatusAgendamento.CONFIRMADO;
    }
}
