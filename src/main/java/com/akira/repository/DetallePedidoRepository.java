package com.akira.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.akira.model.DetallePedido;

@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Integer> {
    
    @Query("SELECT dp FROM DetallePedido dp WHERE dp.orden.id = :ordenId")
    List<DetallePedido> findByOrdenId(@Param("ordenId") Integer ordenId);
    
    @Query("SELECT dp FROM DetallePedido dp WHERE dp.producto.idProducto = :productoId")
    List<DetallePedido> findByProductoId(@Param("productoId") Integer productoId);
    
    @Query("SELECT COUNT(dp) FROM DetallePedido dp WHERE dp.orden.id = :ordenId")
    long countByOrdenId(@Param("ordenId") Integer ordenId);
    
    @Query("SELECT COALESCE(SUM(dp.cantidad), 0) FROM DetallePedido dp WHERE dp.orden.id = :ordenId")
    Integer getTotalCantidadByOrdenId(@Param("ordenId") Integer ordenId);
    
    @Query("SELECT dp.producto.idProducto, SUM(dp.cantidad) as total " +
           "FROM DetallePedido dp " +
           "GROUP BY dp.producto.idProducto " +
           "ORDER BY total DESC")
    List<Object[]> getProductosMasVendidos();
    
    @Query("SELECT dp FROM DetallePedido dp " +
           "JOIN FETCH dp.producto p " +
           "JOIN FETCH dp.orden o " +
           "WHERE dp.orden.id = :ordenId")
    List<DetallePedido> findByOrdenIdWithProductoAndOrden(@Param("ordenId") Integer ordenId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM DetallePedido dp WHERE dp.orden.id = :ordenId")
    void deleteByOrdenId(@Param("ordenId") Integer ordenId);
    
    @Query("SELECT COUNT(dp) > 0 FROM DetallePedido dp WHERE dp.producto.idProducto = :productoId")
    boolean existsByProductoId(@Param("productoId") Integer productoId);
}