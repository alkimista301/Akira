package com.akira.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.akira.model.Marca;

public interface MarcaRepository extends JpaRepository<Marca, Integer> {

}
