package com.akira.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.akira.model.Rol;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {
    
    /**
     * Buscar rol por nombre exacto
     */
    Rol findByNombre(String nombre);
    
    /**
     * Buscar rol por nombre ignorando mayúsculas/minúsculas
     */
    Rol findByNombreIgnoreCase(String nombre);
    
    /**
     * Buscar roles activos
     */
    @Query("SELECT r FROM Rol r WHERE r.activo = true")
    List<Rol> findByActivo();
    
    /**
     * Buscar roles inactivos
     */
    @Query("SELECT r FROM Rol r WHERE r.activo = false")
    List<Rol> findByInactivo();
    
    /**
     * Buscar roles por descripción que contenga el texto
     */
    List<Rol> findByDescripcionContainingIgnoreCase(String descripcion);
    
    /**
     * Verificar si existe rol por nombre
     */
    boolean existsByNombre(String nombre);
    
    /**
     * Verificar si existe rol por nombre ignorando mayúsculas/minúsculas
     */
    boolean existsByNombreIgnoreCase(String nombre);
    
    /**
     * Contar roles activos
     */
    @Query("SELECT COUNT(r) FROM Rol r WHERE r.activo = true")
    long countByActivos();
    
    /**
     * Listar todos los roles ordenados por nombre
     */
    @Query("SELECT r FROM Rol r ORDER BY r.nombre ASC")
    List<Rol> findAllOrderByNombre();
    
    /**
     * Buscar roles activos ordenados por nombre
     */
    @Query("SELECT r FROM Rol r WHERE r.activo = true ORDER BY r.nombre ASC")
    List<Rol> findActivosOrderByNombre();
    
    /**
     * Buscar rol específico para empleados (no clientes)
     */
    @Query("SELECT r FROM Rol r WHERE r.nombre IN ('DUEÑO', 'VENDEDOR', 'TECNICO') AND r.activo = true")
    List<Rol> findRolesEmpleados();
    
    /**
     * Buscar rol de cliente
     */
    @Query("SELECT r FROM Rol r WHERE r.nombre = 'CLIENTE' AND r.activo = true")
    Rol findRolCliente();
}