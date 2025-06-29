package com.akira.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.akira.model.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
}