package com.akira.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.akira.model.Banco;

@Repository
public interface BancoRepository extends JpaRepository<Banco, Integer> {
    
    /**
     * Buscar banco por nombre exacto
     */
    Banco findByNombre(String nombre);
    
    /**
     * Buscar bancos por nombre que contenga el texto (ignorando mayúsculas/minúsculas)
     */
    List<Banco> findByNombreContainingIgnoreCase(String nombre);
    
    /**
     * Verificar si existe banco por nombre
     */
    boolean existsByNombre(String nombre);
    
    /**
     * Buscar bancos ordenados por nombre
     */
    @Query("SELECT b FROM Banco b ORDER BY b.nombre ASC")
    List<Banco> findAllOrderByNombre();
    
    /**
     * Contar bancos por nombre que contenga el texto
     */
    @Query("SELECT COUNT(b) FROM Banco b WHERE LOWER(b.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    long countByNombreContaining(@Param("nombre") String nombre);
}