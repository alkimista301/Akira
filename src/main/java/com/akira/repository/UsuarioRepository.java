package com.akira.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.akira.model.Usuario;
import com.akira.model.Rol;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    
    /**
     * Buscar usuario por nombre de usuario para login
     */
    Optional<Usuario> findByUsuario(String usuario);
    
    /**
     * Buscar usuario por correo electrónico
     */
    Optional<Usuario> findByCorreo(String correo);
    
    /**
     * Buscar usuario por celular
     */
    Optional<Usuario> findByCelular(String celular);
    
    /**
     * Buscar usuarios por rol
     */
    @Query("SELECT u FROM Usuario u WHERE u.rol = :rol AND u.activo = true")
    List<Usuario> findByRol(@Param("rol") Rol rol);
    
    /**
     * Buscar usuarios por ID de rol
     */
    @Query("SELECT u FROM Usuario u WHERE u.rol.id = :rolId AND u.activo = true")
    List<Usuario> findByRolId(@Param("rolId") Integer rolId);
    
    /**
     * Buscar usuarios por nombre de rol
     */
    @Query("SELECT u FROM Usuario u WHERE u.rol.nombre = :nombreRol AND u.activo = true")
    List<Usuario> findByRolNombre(@Param("nombreRol") String nombreRol);
    
    /**
     * Buscar usuarios activos
     */
    @Query("SELECT u FROM Usuario u WHERE u.activo = true")
    List<Usuario> findByActivo();
    
    /**
     * Buscar usuarios por nombre que contenga el texto
     */
    List<Usuario> findByNombreContainingIgnoreCase(String nombre);
    
    /**
     * Buscar usuarios por apellido que contenga el texto
     */
    List<Usuario> findByApellidoContainingIgnoreCase(String apellido);
    
    /**
     * Buscar usuarios por nombre o apellido
     */
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR LOWER(u.apellido) LIKE LOWER(CONCAT('%', :texto, '%'))")
    List<Usuario> findByNombreOrApellidoContaining(@Param("texto") String texto);
    
    /**
     * Buscar usuarios por nombre y apellido exactos
     */
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.nombre) = LOWER(:nombre) AND LOWER(u.apellido) = LOWER(:apellido)")
    List<Usuario> findByNombreAndApellido(@Param("nombre") String nombre, @Param("apellido") String apellido);
    
    /**
     * Buscar todos los clientes activos
     */
    @Query("SELECT u FROM Usuario u WHERE u.rol.nombre = 'CLIENTE' AND u.activo = true ORDER BY u.nombre ASC, u.apellido ASC")
    List<Usuario> findAllClientes();
    
    /**
     * Buscar todos los empleados activos (vendedores, técnicos, dueños)
     */
    @Query("SELECT u FROM Usuario u WHERE u.rol.nombre IN ('DUEÑO', 'VENDEDOR', 'TECNICO') AND u.activo = true ORDER BY u.rol.nombre ASC, u.nombre ASC")
    List<Usuario> findAllEmpleados();
    
    /**
     * Buscar técnicos activos
     */
    @Query("SELECT u FROM Usuario u WHERE u.rol.nombre = 'TECNICO' AND u.activo = true ORDER BY u.nombre ASC")
    List<Usuario> findTecnicos();
    
    /**
     * Buscar vendedores activos
     */
    @Query("SELECT u FROM Usuario u WHERE u.rol.nombre = 'VENDEDOR' AND u.activo = true ORDER BY u.nombre ASC")
    List<Usuario> findVendedores();
    
    /**
     * Verificar si existe usuario por nombre de usuario
     */
    boolean existsByUsuario(String usuario);
    
    /**
     * Verificar si existe usuario por correo
     */
    boolean existsByCorreo(String correo);
    
    /**
     * Verificar si existe usuario por celular
     */
    boolean existsByCelular(String celular);
    
    /**
     * Buscar usuarios con correo registrado
     */
    @Query("SELECT u FROM Usuario u WHERE u.correo IS NOT NULL AND u.correo <> '' AND u.activo = true")
    List<Usuario> findUsuariosConCorreo();
    
    /**
     * Buscar usuarios con celular registrado
     */
    @Query("SELECT u FROM Usuario u WHERE u.celular IS NOT NULL AND u.celular <> '' AND u.activo = true")
    List<Usuario> findUsuariosConCelular();
    
    /**
     * Buscar usuarios con acceso al sistema (tienen usuario y password)
     */
    @Query("SELECT u FROM Usuario u WHERE u.usuario IS NOT NULL AND u.password IS NOT NULL AND u.activo = true")
    List<Usuario> findUsuariosConAcceso();
    
    /**
     * Contar usuarios por rol
     */
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.rol.nombre = :nombreRol AND u.activo = true")
    long countByRolNombre(@Param("nombreRol") String nombreRol);
    
    /**
     * Buscar usuarios ordenados por nombre completo
     */
    @Query("SELECT u FROM Usuario u WHERE u.activo = true ORDER BY u.nombre ASC, u.apellido ASC")
    List<Usuario> findAllOrderByNombreCompleto();
}