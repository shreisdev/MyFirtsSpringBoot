package com.example.demo.repository;

import com.example.demo.model.Barbeiro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BarbeiroRepository extends JpaRepository<Barbeiro, Long> {

    Optional<Barbeiro> findByNomeUsuario(String nomeUsuario);

    boolean existsByNomeUsuario(String nomeUsuario);
}
