package com.akira.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.akira.model.OrdenPedido;
import com.akira.model.Usuario;
import com.akira.model.Estado;

@Repository
public interface OrdenPedidoRepository extends JpaRepository<OrdenPedido, Integer> {
    
    @Query("SELECT op FROM OrdenPedido op WHERE op.cliente.id = :clienteId ORDER BY op.fecha DESC")
    List<OrdenPedido> findByClienteId(@Param("clienteId") String clienteId);
    
    @Query("SELECT op FROM OrdenPedido op WHERE op.estado.descripcion = :estadoDescripcion ORDER BY op.fecha ASC")
    List<OrdenPedido> findByEstadoDescripcion(@Param("estadoDescripcion") String estadoDescripcion);
    
    @Query("SELECT op FROM OrdenPedido op WHERE op.tecnicoAsignado.id = :tecnicoId ORDER BY op.fecha DESC")
    List<OrdenPedido> findByTecnicoAsignadoId(@Param("tecnicoId") String tecnicoId);
    
    @Query("SELECT op FROM OrdenPedido op WHERE op.vendedorAsignado.id = :vendedorId ORDER BY op.fecha DESC")
    List<OrdenPedido> findByVendedorAsignadoId(@Param("vendedorId") String vendedorId);
    
    @Query("SELECT op FROM OrdenPedido op WHERE op.estado.descripcion = 'PENDIENTE' AND op.tecnicoAsignado IS NULL ORDER BY op.fecha ASC")
    List<OrdenPedido> findPedidosPendientesSinAsignar();
    
    @Query("SELECT op FROM OrdenPedido op WHERE op.estado.descripcion = 'ASIGNADO' AND op.tecnicoAsignado.id = :tecnicoId ORDER BY op.fecha ASC")
    List<OrdenPedido> findPedidosAsignadosATecnico(@Param("tecnicoId") String tecnicoId);
    
    @Query("SELECT op FROM OrdenPedido op WHERE op.estado.descripcion = 'ATENDIDO' AND op.vendedorAsignado.id = :vendedorId ORDER BY op.fechaAtencion DESC")
    List<OrdenPedido> findPedidosAtendidosDeVendedor(@Param("vendedorId") String vendedorId);
    
    @Query("SELECT op FROM OrdenPedido op WHERE op.estado.descripcion = 'ATENDIDO' ORDER BY op.fechaAtencion DESC")
    List<OrdenPedido> findTodosPedidosAtendidos();
    
    @Query("SELECT op FROM OrdenPedido op WHERE op.estado.descripcion = 'CERRADO' AND op.vendedorAsignado.id = :vendedorId ORDER BY op.fechaCierre DESC")
    List<OrdenPedido> findPedidosCerradosDeVendedor(@Param("vendedorId") String vendedorId);
    
    @Query("SELECT op FROM OrdenPedido op WHERE op.fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY op.fecha DESC")
    List<OrdenPedido> findByFechaBetween(@Param("fechaInicio") LocalDateTime fechaInicio, @Param("fechaFin") LocalDateTime fechaFin);
    
    @Query("SELECT op FROM OrdenPedido op WHERE op.cliente.nombre LIKE %:nombre% OR op.cliente.apellido LIKE %:nombre% ORDER BY op.fecha DESC")
    List<OrdenPedido> findByClienteNombreContaining(@Param("nombre") String nombre);
    
    @Query("SELECT op FROM OrdenPedido op WHERE op.total >= :montoMinimo ORDER BY op.total DESC")
    List<OrdenPedido> findByTotalMayorIgual(@Param("montoMinimo") Double montoMinimo);
    
    @Query("SELECT op FROM OrdenPedido op WHERE op.estado.descripcion IN :estados ORDER BY op.fecha DESC")
    List<OrdenPedido> findByEstadosIn(@Param("estados") List<String> estados);
    
    @Query("SELECT op FROM OrdenPedido op WHERE op.tecnicoAsignado IS NULL AND op.estado.descripcion = 'PENDIENTE' ORDER BY op.fecha ASC")
    List<OrdenPedido> findPedidosSinTecnicoAsignado();
    
    @Query("SELECT op FROM OrdenPedido op WHERE op.vendedorAsignado IS NULL AND op.estado.descripcion IN ('PENDIENTE', 'ASIGNADO') ORDER BY op.fecha ASC")
    List<OrdenPedido> findPedidosSinVendedorAsignado();
    
    @Query("SELECT COUNT(op) FROM OrdenPedido op WHERE op.estado.descripcion = :estadoDescripcion")
    long countByEstadoDescripcion(@Param("estadoDescripcion") String estadoDescripcion);
    
    @Query("SELECT COUNT(op) FROM OrdenPedido op WHERE op.tecnicoAsignado.id = :tecnicoId AND op.estado.descripcion = 'ASIGNADO'")
    long countPedidosAsignadosATecnico(@Param("tecnicoId") String tecnicoId);
    
    @Query("SELECT COUNT(op) FROM OrdenPedido op WHERE op.vendedorAsignado.id = :vendedorId AND op.estado.descripcion = 'ATENDIDO'")
    long countPedidosAtendidosDeVendedor(@Param("vendedorId") String vendedorId);
    
    @Query("SELECT COUNT(op) FROM OrdenPedido op WHERE op.cliente.id = :clienteId")
    long countByClienteId(@Param("clienteId") String clienteId);
    
    @Query("SELECT op FROM OrdenPedido op WHERE op.observaciones IS NOT NULL AND op.observaciones <> '' ORDER BY op.fechaAtencion DESC")
    List<OrdenPedido> findPedidosConObservaciones();
    
    @Query("SELECT op FROM OrdenPedido op WHERE op.fecha >= :fechaInicio ORDER BY op.fecha DESC")
    List<OrdenPedido> findPedidosRecientes(@Param("fechaInicio") LocalDateTime fechaInicio);
    
    @Query("SELECT op FROM OrdenPedido op WHERE op.estado.descripcion NOT IN ('CERRADO') ORDER BY op.fecha ASC")
    List<OrdenPedido> findPedidosActivos();
    
    @Query("SELECT DISTINCT op.cliente FROM OrdenPedido op WHERE op.fecha >= :fechaInicio")
    List<Usuario> findClientesConPedidosRecientes(@Param("fechaInicio") LocalDateTime fechaInicio);
    
    @Query("SELECT op FROM OrdenPedido op WHERE op.tecnicoAsignado.id = :tecnicoId AND op.estado.descripcion = 'ASIGNADO' AND op.fechaAsignacion <= :fechaLimite")
    List<OrdenPedido> findPedidosAsignadosVencidos(@Param("tecnicoId") String tecnicoId, @Param("fechaLimite") LocalDateTime fechaLimite);
    
    @Query("SELECT SUM(op.total) FROM OrdenPedido op WHERE op.estado.descripcion = 'CERRADO' AND op.fecha BETWEEN :fechaInicio AND :fechaFin")
    Double sumTotalVentasPorPeriodo(@Param("fechaInicio") LocalDateTime fechaInicio, @Param("fechaFin") LocalDateTime fechaFin);
    
    @Query("SELECT SUM(op.total) FROM OrdenPedido op WHERE op.vendedorAsignado.id = :vendedorId AND op.estado.descripcion = 'CERRADO' AND op.fecha BETWEEN :fechaInicio AND :fechaFin")
    Double sumVentasVendedorPorPeriodo(@Param("vendedorId") String vendedorId, @Param("fechaInicio") LocalDateTime fechaInicio, @Param("fechaFin") LocalDateTime fechaFin);
    
    @Query("SELECT op FROM OrdenPedido op ORDER BY op.fecha DESC")
    List<OrdenPedido> findAllOrderByFechaDesc();
    
    
     @Query("SELECT op FROM OrdenPedido op WHERE op.tipoPedido = :tipoPedido ORDER BY op.fecha DESC")
     List<OrdenPedido> findByTipoPedido(@Param("tipoPedido") String tipoPedido);
     
     @Query("SELECT op FROM OrdenPedido op WHERE op.tipoPedido = :tipoPedido AND op.estado.descripcion = :estadoDescripcion ORDER BY op.fecha DESC")
     List<OrdenPedido> findByTipoPedidoAndEstadoDescripcion(@Param("tipoPedido") String tipoPedido, @Param("estadoDescripcion") String estadoDescripcion);
     
     
     @Query("SELECT op FROM OrdenPedido op WHERE op.cliente.dni = :clienteDni ORDER BY op.fecha DESC")
     List<OrdenPedido> findByClienteDniOrderByFechaDesc(@Param("clienteDni") String clienteDni);
}