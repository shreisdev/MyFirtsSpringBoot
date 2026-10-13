package com.example.demo.repository;

import com.example.demo.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {

    List<Servico> findByAtivoTrueOrderByNomeAsc();

    List<Servico> findAllByOrderByNomeAsc();

    boolean existsByNomeIgnoreCase(String nome);
}
