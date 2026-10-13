package com.example.demo.repository;

import com.example.demo.model.Agendamento;
import com.example.demo.model.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    /** Busca pelo codigo (token) enviado ao cliente. */
    Optional<Agendamento> findByTokenAcesso(String tokenAcesso);

    /** RF07: ja existe agendamento do barbeiro nesse exato horario? */
    boolean existsByBarbeiroIdAndDataHora(Long barbeiroId, LocalDateTime dataHora);

    List<Agendamento> findAllByOrderByDataHoraAsc();

    List<Agendamento> findByDataHoraBetweenOrderByDataHoraAsc(LocalDateTime inicio, LocalDateTime fim);

    List<Agendamento> findByBarbeiroIdAndDataHoraBetweenOrderByDataHoraAsc(
            Long barbeiroId, LocalDateTime inicio, LocalDateTime fim);

    List<Agendamento> findByBarbeiroIdOrderByDataHoraAsc(Long barbeiroId);

    /** RF06: quantos agendamentos ativos um e-mail possui? */
    long countByEmailClienteIgnoreCaseAndStatus(String emailCliente, StatusAgendamento status);

    List<Agendamento> findByEmailClienteIgnoreCaseOrderByDataHoraDesc(String emailCliente);
}
