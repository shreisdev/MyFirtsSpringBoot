package com.example.demo.exception;

/**
 * Lancada quando um recurso nao e encontrado (ex.: agendamento inexistente).
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
