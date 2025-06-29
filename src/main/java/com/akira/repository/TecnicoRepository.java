package com.akira.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.akira.model.Tecnico;

public interface TecnicoRepository extends JpaRepository<Tecnico, Integer> {

    // Buscar por DNI exacto
    Tecnico findByDni(String dni);
    
    // Buscar por nombre (contiene, ignora mayúsculas)
    List<Tecnico> findByNombreContainingIgnoreCase(String nombre);
    
    // Buscar por apellido (contiene, ignora mayúsculas)
    List<Tecnico> findByApellidoContainingIgnoreCase(String apellido);
    
    // Buscar por nombre Y apellido
    @Query("SELECT t FROM Tecnico t WHERE t.nombre LIKE %:nombre% AND t.apellido LIKE %:apellido%")
    List<Tecnico> findByNombreAndApellido(@Param("nombre") String nombre, @Param("apellido") String apellido);
    
    // Contar técnicos activos (todos por ahora)
    @Query("SELECT COUNT(t) FROM Tecnico t")
    long countTotalTecnicos();
    
    // Buscar técnicos por nombre completo
    @Query("SELECT t FROM Tecnico t WHERE CONCAT(t.nombre, ' ', t.apellido) LIKE %:nombreCompleto%")
    List<Tecnico> findByNombreCompletoContaining(@Param("nombreCompleto") String nombreCompleto);
    
    // Verificar si existe por DNI
    boolean existsByDni(String dni);
}