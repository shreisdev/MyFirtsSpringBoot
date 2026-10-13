package com.example.demo.repository;

import com.example.demo.model.BloqueioHorario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BloqueioHorarioRepository extends JpaRepository<BloqueioHorario, Long> {

    List<BloqueioHorario> findByBarbeiroIdOrderByDataHoraInicioAsc(Long barbeiroId);

    void deleteByBarbeiroId(Long barbeiroId);
}
