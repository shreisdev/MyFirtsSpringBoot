package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Faixa de horario em que o barbeiro trabalha em determinado dia da semana.
 *
 * {@code diaSemana} segue o padrao ISO-8601:
 * 1 = segunda, 2 = terca, ..., 6 = sabado, 7 = domingo.
 */
@Entity
@Table(name = "horario_trabalho")
@Getter
@Setter
@NoArgsConstructor
public class HorarioTrabalho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dia_semana", nullable = false)
    private int diaSemana;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fim", nullable = false)
    private LocalTime horaFim;

    @Column(name = "inicio_almoco")
    private LocalTime inicioAlmoco;

    @Column(name = "fim_almoco")
    private LocalTime fimAlmoco;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "barbeiro_id", nullable = false)
    private Barbeiro barbeiro;

    /**
     * O horario de trabalho cobre o intervalo pedido?
     * Considera o dia da semana, a faixa de trabalho e o intervalo de almoco.
     */
    public boolean cobre(LocalDateTime dataHora, int duracaoMinutos) {
        if (dataHora == null || duracaoMinutos <= 0) {
            return false;
        }
        if (dataHora.getDayOfWeek().getValue() != this.diaSemana) {
            return false;
        }

        LocalTime inicio = dataHora.toLocalTime();
        LocalTime fim = inicio.plusMinutes(duracaoMinutos);

        if (inicio.isBefore(horaInicio) || fim.isAfter(horaFim)) {
            return false;
        }

        if (inicioAlmoco != null && fimAlmoco != null) {
            // nao pode atravessar / cair dentro do almoco
            return !fim.isAfter(inicioAlmoco) || !inicio.isBefore(fimAlmoco);
        }
        return true;
    }
}
