package com.akira.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.akira.model.Estado;

public interface EstadoRepository extends JpaRepository<Estado, Integer> {

    // Buscar por descripción exacta
    Estado findByDescripcion(String descripcion);
    
    // Verificar si existe por descripción
    boolean existsByDescripcion(String descripcion);
    
    // Contar total de estados
    @Query("SELECT COUNT(e) FROM Estado e")
    long countTotalEstados();
}