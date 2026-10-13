package com.example.demo.repository;

import com.example.demo.model.HorarioTrabalho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HorarioTrabalhoRepository extends JpaRepository<HorarioTrabalho, Long> {

    List<HorarioTrabalho> findByBarbeiroIdOrderByDiaSemanaAsc(Long barbeiroId);

    List<HorarioTrabalho> findByBarbeiroIdAndDiaSemana(Long barbeiroId, int diaSemana);

    void deleteByBarbeiroId(Long barbeiroId);
}
