package com.akira.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.akira.model.RegistroPago;

@Repository
public interface RegistroPagoRepository extends JpaRepository<RegistroPago, Integer> {
    
    /**
     * Buscar pagos por ID de orden
     */
    @Query("SELECT rp FROM RegistroPago rp WHERE rp.orden.id = :idOrden")
    List<RegistroPago> findByOrdenId(@Param("idOrden") Integer idOrden);
    
    /**
     * Buscar pago por número de operación
     */
    RegistroPago findByNroOperacion(String nroOperacion);
    
    /**
     * Verificar si existe número de operación
     */
    boolean existsByNroOperacion(String nroOperacion);
    
    /**
     * Buscar pagos por banco
     */
    List<RegistroPago> findByBancoId(Integer bancoId);
    
    /**
     * Buscar pagos por estado
     */
    List<RegistroPago> findByEstadoId(Integer estadoId);
}