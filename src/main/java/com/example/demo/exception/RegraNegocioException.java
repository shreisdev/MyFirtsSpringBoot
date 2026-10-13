package com.example.demo.exception;

/**
 * Excecao para violacao de regras de negocio (RF06, RF07, RF20, etc).
 * Os controllers capturam e devolvem uma mensagem amigavel ao usuario.
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
