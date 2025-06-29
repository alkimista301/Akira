package com.akira.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.akira.model.Vendedor;

public interface VendedorRepository extends JpaRepository<Vendedor, Integer> {

    // Buscar por DNI exacto
    Vendedor findByDni(String dni);
    
    // Buscar por nombre (contiene, ignora mayúsculas)
    List<Vendedor> findByNombreContainingIgnoreCase(String nombre);
    
    // Buscar por apellido (contiene, ignora mayúsculas)
    List<Vendedor> findByApellidoContainingIgnoreCase(String apellido);
    
    // Buscar por nombre Y apellido
    @Query("SELECT v FROM Vendedor v WHERE v.nombre LIKE %:nombre% AND v.apellido LIKE %:apellido%")
    List<Vendedor> findByNombreAndApellido(@Param("nombre") String nombre, @Param("apellido") String apellido);
    
    // Contar vendedores activos (todos por ahora)
    @Query("SELECT COUNT(v) FROM Vendedor v")
    long countTotalVendedores();
    
    // Buscar vendedores por nombre completo
    @Query("SELECT v FROM Vendedor v WHERE CONCAT(v.nombre, ' ', v.apellido) LIKE %:nombreCompleto%")
    List<Vendedor> findByNombreCompletoContaining(@Param("nombreCompleto") String nombreCompleto);
}
