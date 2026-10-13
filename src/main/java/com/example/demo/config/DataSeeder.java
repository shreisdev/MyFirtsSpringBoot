package com.example.demo.config;

import com.example.demo.model.Barbeiro;
import com.example.demo.model.HorarioTrabalho;
import com.example.demo.model.Servico;
import com.example.demo.repository.BarbeiroRepository;
import com.example.demo.repository.HorarioTrabalhoRepository;
import com.example.demo.repository.ServicoRepository;
import com.example.demo.util.SenhaUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * Popula o banco com dados iniciais para o projeto rodar de imediato.
 *
 * Cria:
 * - barbeiro administrador (usuario "admin" / senha "admin"), igual ao texto do template de login;
 * - um segundo barbeiro de exemplo;
 * - horarios de trabalho de segunda a sabado (com almoco);
 * - tres servicos.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final BarbeiroRepository barbeiroRepository;
    private final HorarioTrabalhoRepository horarioTrabalhoRepository;
    private final ServicoRepository servicoRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (barbeiroRepository.count() > 0) {
            return; // banco ja populado
        }

        Barbeiro admin = criarBarbeiro("Administrador", "admin", "admin");
        Barbeiro joao = criarBarbeiro("João Barbeiro", "joao", "joao");

        criarHorarios(admin);
        criarHorarios(joao);

        servicoRepository.save(criarServico("Corte de Cabelo", 30, "50.00"));
        servicoRepository.save(criarServico("Barba", 20, "35.00"));
        servicoRepository.save(criarServico("Corte + Barba", 50, "75.00"));
    }

    private Barbeiro criarBarbeiro(String nome, String nomeUsuario, String senha) {
        Barbeiro barbeiro = new Barbeiro();
        barbeiro.setNome(nome);
        barbeiro.setNomeUsuario(nomeUsuario);
        barbeiro.setSenhaHash(SenhaUtil.hash(senha));
        barbeiro.setTentativasLoginFalhas(0);
        return barbeiroRepository.save(barbeiro);
    }

    /** Segunda a sexta: 09:00-19:00 com almoco 12:00-13:00. Sabado: 09:00-14:00. */
    private void criarHorarios(Barbeiro barbeiro) {
        for (int dia = 1; dia <= 5; dia++) {
            criarHorario(barbeiro, dia, "09:00", "19:00", "12:00", "13:00");
        }
        criarHorario(barbeiro, 6, "09:00", "14:00", null, null);
    }

    private void criarHorario(Barbeiro barbeiro, int diaSemana, String inicio, String fim,
                              String inicioAlmoco, String fimAlmoco) {
        HorarioTrabalho horario = new HorarioTrabalho();
        horario.setBarbeiro(barbeiro);
        horario.setDiaSemana(diaSemana);
        horario.setHoraInicio(LocalTime.parse(inicio));
        horario.setHoraFim(LocalTime.parse(fim));
        if (inicioAlmoco != null) {
            horario.setInicioAlmoco(LocalTime.parse(inicioAlmoco));
        }
        if (fimAlmoco != null) {
            horario.setFimAlmoco(LocalTime.parse(fimAlmoco));
        }
        horarioTrabalhoRepository.save(horario);
    }

    private Servico criarServico(String nome, int duracaoMinutos, String preco) {
        Servico servico = new Servico();
        servico.setNome(nome);
        servico.setDuracaoMinutos(duracaoMinutos);
        servico.setPreco(new BigDecimal(preco));
        servico.setAtivo(true);
        return servico;
    }
}
