package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Bloqueio na agenda do barbeiro (almoco estendido, ferias, folga, compromisso...).
 *
 * PONTUAL: vale apenas entre {@code dataHoraInicio} e {@code dataHoraFim}.
 * RECORRENTE: repete toda semana no {@code diaSemanaRecorrencia}
 *             (ISO-8601: 1 = segunda ... 7 = domingo), usando o horario de
 *             inicio/fim do bloqueio.
 */
@Entity
@Table(name = "bloqueio_horario")
@Getter
@Setter
@NoArgsConstructor
public class BloqueioHorario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoBloqueio tipo;

    @Column(name = "data_hora_inicio", nullable = false)
    private LocalDateTime dataHoraInicio;

    @Column(name = "data_hora_fim", nullable = false)
    private LocalDateTime dataHoraFim;

    /** Usado apenas quando o tipo for RECORRENTE. */
    @Column(name = "dia_semana_recorrencia")
    private Integer diaSemanaRecorrencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barbeiro_id")
    private Barbeiro barbeiro;

    /** O bloqueio abrange este instante? */
    public boolean abrange(LocalDateTime dataHora) {
        if (dataHora == null || dataHoraInicio == null || dataHoraFim == null) {
            return false;
        }

        if (tipo == TipoBloqueio.PONTUAL) {
            return !dataHora.isBefore(dataHoraInicio) && !dataHora.isAfter(dataHoraFim);
        }

        // RECORRENTE
        if (diaSemanaRecorrencia != null && dataHora.getDayOfWeek().getValue() != diaSemanaRecorrencia) {
            return false;
        }
        LocalTime hora = dataHora.toLocalTime();
        LocalTime inicio = dataHoraInicio.toLocalTime();
        LocalTime fim = dataHoraFim.toLocalTime();
        return !hora.isBefore(inicio) && !hora.isAfter(fim);
    }
}
