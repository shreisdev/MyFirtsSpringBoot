package com.example.demo.util;

import jakarta.servlet.http.HttpSession;

/**
 * Apoio para as telas administrativas (login do barbeiro).
 *
 * O atributo de sessao e o mesmo gravado pelo {@code AdminController}
 * ("barbeiroLogadoId"), entao o acesso e compartilhado entre os controllers.
 */
public final class SessaoAdmin {

    /** Mesma chave usada em {@code AdminController.SESSION_BARBEIRO_ID}. */
    public static final String ATRIBUTO_BARBEIRO = "barbeiroLogadoId";

    private SessaoAdmin() {
    }

    /** Existe um barbeiro logado nesta sessao? */
    public static boolean estaLogado(HttpSession session) {
        return session != null && session.getAttribute(ATRIBUTO_BARBEIRO) != null;
    }

    /** Id do barbeiro logado, ou {@code null} se ninguem estiver logado. */
    public static Long barbeiroId(HttpSession session) {
        Object valor = (session == null) ? null : session.getAttribute(ATRIBUTO_BARBEIRO);
        return (valor instanceof Long id) ? id : null;
    }
}
