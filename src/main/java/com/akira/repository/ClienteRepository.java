package com.akira.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.akira.model.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, String> {
    
    /**
     * Buscar clientes activos
     */
    List<Cliente> findByActivoTrue();
    
    /**
     * Buscar clientes por nombre que contenga el texto (ignorando mayúsculas/minúsculas)
     */
    List<Cliente> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);
    
    /**
     * Buscar clientes por apellido que contenga el texto (ignorando mayúsculas/minúsculas)
     */
    List<Cliente> findByApellidoContainingIgnoreCaseAndActivoTrue(String apellido);
    
    /**
     * Buscar clientes por nombre o apellido (contiene el texto)
     */
    @Query("SELECT c FROM Cliente c WHERE c.activo = true AND " +
           "(LOWER(c.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(c.apellido) LIKE LOWER(CONCAT('%', :texto, '%'))) " +
           "ORDER BY c.nombre ASC, c.apellido ASC")
    List<Cliente> findByNombreOrApellidoContaining(@Param("texto") String texto);
    
    /**
     * Buscar clientes por nombre y apellido exactos
     */
    @Query("SELECT c FROM Cliente c WHERE c.activo = true AND " +
           "LOWER(c.nombre) = LOWER(:nombre) AND LOWER(c.apellido) = LOWER(:apellido)")
    List<Cliente> findByNombreAndApellidoExact(@Param("nombre") String nombre, @Param("apellido") String apellido);
    
    /**
     * Buscar cliente por correo electrónico
     */
    Cliente findByCorreoAndActivoTrue(String correo);
    
    /**
     * Buscar cliente por número de celular
     */
    Cliente findByCelularAndActivoTrue(String celular);
    
    /**
     * Buscar clientes ordenados por nombre completo
     */
    @Query("SELECT c FROM Cliente c WHERE c.activo = true ORDER BY c.nombre ASC, c.apellido ASC")
    List<Cliente> findAllActivosOrdenados();
    
    /**
     * Verificar si existe cliente por correo
     */
    boolean existsByCorreo(String correo);
    
    /**
     * Verificar si existe cliente por celular
     */
    boolean existsByCelular(String celular);
    
    /**
     * Contar clientes activos
     */
    long countByActivoTrue();
    
    /**
     * Buscar clientes por nombre que contenga el texto (incluye inactivos)
     */
    @Query("SELECT c FROM Cliente c WHERE " +
           "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(c.apellido) LIKE LOWER(CONCAT('%', :texto, '%')) " +
           "ORDER BY c.activo DESC, c.nombre ASC")
    List<Cliente> findByNombreOrApellidoContainingAll(@Param("texto") String texto);
    
    /**
     * Buscar clientes con correo registrado
     */
    @Query("SELECT c FROM Cliente c WHERE c.activo = true AND c.correo IS NOT NULL AND c.correo <> '' " +
           "ORDER BY c.nombre ASC")
    List<Cliente> findClientesConCorreo();
    
    /**
     * Buscar clientes con celular registrado
     */
    @Query("SELECT c FROM Cliente c WHERE c.activo = true AND c.celular IS NOT NULL AND c.celular <> '' " +
           "ORDER BY c.nombre ASC")
    List<Cliente> findClientesConCelular();
    
    /**
     * Buscar clientes con información de contacto completa
     */
    @Query("SELECT c FROM Cliente c WHERE c.activo = true AND " +
           "c.correo IS NOT NULL AND c.correo <> '' AND " +
           "c.celular IS NOT NULL AND c.celular <> '' " +
           "ORDER BY c.nombre ASC")
    List<Cliente> findClientesConContactoCompleto();
    
    /**
     * Buscar clientes por tipo de documento
     */
    @Query("SELECT c FROM Cliente c WHERE c.activo = true AND LENGTH(c.dni) = :longitudDni ORDER BY c.nombre ASC")
    List<Cliente> findByTipoDocumento(@Param("longitudDni") int longitudDni);
    
    /**
     * Buscar solo clientes con DNI (8 dígitos)
     */
    default List<Cliente> findClientesConDNI() {
        return findByTipoDocumento(8);
    }
    
    /**
     * Buscar solo clientes con RUC (11 dígitos)
     */
    default List<Cliente> findClientesConRUC() {
        return findByTipoDocumento(11);
    }
    
    /**
     * Búsqueda avanzada por múltiples criterios
     */
    @Query("SELECT c FROM Cliente c WHERE c.activo = true AND " +
           "(:texto IS NULL OR :texto = '' OR " +
           "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(c.apellido) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "c.dni LIKE CONCAT('%', :texto, '%')) " +
           "ORDER BY c.nombre ASC, c.apellido ASC")
    List<Cliente> busquedaAvanzada(@Param("texto") String texto);
    
    
    /**
     * Buscar cliente por usuario (para login)
     */
    Cliente findByUsuarioAndActivoTrue(String usuario);

    /**
     * Verificar si existe usuario
     */
    boolean existsByUsuario(String usuario);

    /**
     * Buscar cliente por usuario (incluye inactivos)
     */
    Cliente findByUsuario(String usuario);
    
    
   

    /**
     * Verificar si existe cliente por DNI
     */
    boolean existsByDni(String dni);


    /**
     * Listar clientes activos ordenados por nombre
     */
    List<Cliente> findByActivoTrueOrderByNombreAsc();

    /**
     * Buscar clientes por nombre o apellido (búsqueda parcial)
     */
    List<Cliente> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCaseAndActivoTrue(String nombre, String apellido);

   
}