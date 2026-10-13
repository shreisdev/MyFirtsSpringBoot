package com.example.demo.model;

/**
 * Tipo de {@link BloqueioHorario}.
 * Definido no diagrama de classes do projeto.
 */
public enum TipoBloqueio {

    /** Bloqueio de uma data/hora especifica. */
    PONTUAL,

    /** Bloqueio que se repete toda semana em um dia fixo. */
    RECORRENTE
}
