package com.example.demo.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Nomes dos dias da semana no padrao ISO-8601
 * (1 = segunda, 2 = terca, ..., 6 = sabado, 7 = domingo).
 *
 * Usado pelas telas administrativas para exibir o dia a partir do numero
 * guardado em {@code HorarioTrabalho.diaSemana} e
 * {@code BloqueioHorario.diaSemanaRecorrencia}.
 */
public final class DiasSemana {

    private static final Map<Integer, String> NOMES;

    static {
        Map<Integer, String> map = new LinkedHashMap<>();
        map.put(1, "Segunda-feira");
        map.put(2, "Terça-feira");
        map.put(3, "Quarta-feira");
        map.put(4, "Quinta-feira");
        map.put(5, "Sexta-feira");
        map.put(6, "Sábado");
        map.put(7, "Domingo");
        NOMES = Collections.unmodifiableMap(map);
    }

    private DiasSemana() {
    }

    /** Mapa imutavel: numero do dia (1-7) -> nome em portugues. */
    public static Map<Integer, String> nomes() {
        return NOMES;
    }
}
