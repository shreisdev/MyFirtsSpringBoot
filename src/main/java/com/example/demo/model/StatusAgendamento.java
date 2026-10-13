package com.example.demo.model;

/**
 * Status possiveis de um {@link Agendamento}.
 * Definido no diagrama de classes do projeto.
 */
public enum StatusAgendamento {

    /** Agendamento reservado e valido. */
    CONFIRMADO,

    /** Cancelado dentro do prazo permitido. */
    CANCELADO,

    /** Cancelado depois do prazo permitido (menos de 2h antes). */
    CANCELADO_TARDIO,

    /** Atendimento realizado. */
    CONCLUIDO,

    /** Cliente nao compareceu. */
    FALTA
}
