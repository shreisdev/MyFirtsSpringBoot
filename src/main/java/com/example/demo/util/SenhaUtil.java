package com.example.demo.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utilitario para gerar e conferir o hash da senha do barbeiro (campo {@code senhaHash}).
 *
 * Usa BCrypt, que ja guarda o "salt" dentro do proprio hash,
 * portanto nao e preciso um campo separado para o salt.
 */
public final class SenhaUtil {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private SenhaUtil() {
        // classe utilitaria: nao deve ser instanciada
    }

    /** Gera o hash BCrypt de uma senha em texto puro. */
    public static String hash(String senha) {
        return ENCODER.encode(senha);
    }

    /** Confere se a senha em texto puro corresponde ao hash salvo. */
    public static boolean confere(String senha, String senhaHash) {
        if (senha == null || senhaHash == null || senhaHash.isBlank()) {
            return false;
        }
        return ENCODER.matches(senha, senhaHash);
    }
}
