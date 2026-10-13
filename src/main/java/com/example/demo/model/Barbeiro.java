package com.example.demo.model;

import com.example.demo.util.SenhaUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Barbeiro (profissional) da barbearia.
 * Tambem e o usuario que acessa o painel administrativo (RF05).
 */
@Entity
@Table(name = "barbeiro", uniqueConstraints = @UniqueConstraint(name = "uk_barbeiro_nome_usuario", columnNames = "nome_usuario"))
@Getter
@Setter
@NoArgsConstructor
public class Barbeiro {

    /** Apos N tentativas erradas a conta fica bloqueada por alguns minutos. */
    public static final int MAX_TENTATIVAS_LOGIN = 5;
    public static final long MINUTOS_BLOQUEIO = 15;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    /** Nome do arquivo de imagem no servidor (templates usam no cabecalho). */
    private String foto;

    @Column(name = "nome_usuario", nullable = false, unique = true)
    private String nomeUsuario;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Column(name = "tentativas_login_falhas", nullable = false)
    private int tentativasLoginFalhas;

    @Column(name = "bloqueado_ate")
    private LocalDateTime bloqueadoAte;

    @OneToMany(mappedBy = "barbeiro")
    private List<HorarioTrabalho> horariosTrabalho = new ArrayList<>();

    @OneToMany(mappedBy = "barbeiro")
    private List<BloqueioHorario> bloqueios = new ArrayList<>();

    @OneToMany(mappedBy = "barbeiro")
    private List<Agendamento> agendamentos = new ArrayList<>();

    /**
     * Autentica o barbeiro no painel administrativo.
     *
     * Regras:
     * - se a conta estiver bloqueada, recusa o acesso;
     * - se usuario/senha nao conferirem, conta a tentativa falha;
     * - ao acertar, zera as tentativas e o bloqueio.
     *
     * @return true se as credenciais estao corretas
     */
    public boolean autenticar(String usuario, String senha) {
        if (estaBloqueado()) {
            return false;
        }
        boolean usuarioConfere = this.nomeUsuario != null && this.nomeUsuario.equals(usuario);
        boolean senhaConfere = SenhaUtil.confere(senha, this.senhaHash);

        if (!usuarioConfere || !senhaConfere) {
            registrarFalhaLogin();
            return false;
        }

        this.tentativasLoginFalhas = 0;
        this.bloqueadoAte = null;
        return true;
    }

    private void registrarFalhaLogin() {
        this.tentativasLoginFalhas++;
        if (this.tentativasLoginFalhas >= MAX_TENTATIVAS_LOGIN) {
            this.bloqueadoAte = LocalDateTime.now().plusMinutes(MINUTOS_BLOQUEIO);
            this.tentativasLoginFalhas = 0;
        }
    }

    /** A conta esta temporariamente bloqueada por excesso de tentativas? */
    public boolean estaBloqueado() {
        return bloqueadoAte != null && bloqueadoAte.isAfter(LocalDateTime.now());
    }

    /**
     * Verifica se o barbeiro consegue atender um servico de {@code duracaoMinutos}
     * minutos comecando em {@code dataHora}.
     *
     * Regras verificadas (RF07):
     * 1. o horario nao pode estar no passado;
     * 2. o periodo precisa estar dentro de um {@link HorarioTrabalho} (respeitando o almoco);
     * 3. o periodo nao pode estar dentro de um {@link BloqueioHorario};
     * 4. nao pode existir outro agendamento CONFIRMADO que se sobreponha.
     */
    public boolean estaDisponivel(LocalDateTime dataHora, int duracaoMinutos) {
        if (dataHora == null || duracaoMinutos <= 0) {
            return false;
        }
        if (dataHora.isBefore(LocalDateTime.now())) {
            return false;
        }

        boolean dentroDoHorarioTrabalho = horariosTrabalho.stream()
                .anyMatch(horario -> horario.cobre(dataHora, duracaoMinutos));
        if (!dentroDoHorarioTrabalho) {
            return false;
        }

        LocalDateTime fim = dataHora.plusMinutes(duracaoMinutos);
        boolean emBloqueio = bloqueios.stream()
                .anyMatch(bloqueio -> bloqueio.abrange(dataHora) || bloqueio.abrange(fim.minusSeconds(1)));
        if (emBloqueio) {
            return false;
        }

        return agendamentos.stream()
                .filter(Agendamento::estaAtivo)
                .noneMatch(agendamento -> agendamento.conflitaCom(dataHora, duracaoMinutos));
    }
}
