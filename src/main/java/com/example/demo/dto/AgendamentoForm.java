package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * Objeto usado pelo formulario de agendamento (templates/agendar/index.html).
 * O atributo "agendamentoForm" e referenciado no th:object do template.
 */
@Getter
@Setter
public class AgendamentoForm {

    private Long barbeiroId;

    private Long servicoId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime horario;

    private String nome;

    private String telefone;

    private String email;
}
