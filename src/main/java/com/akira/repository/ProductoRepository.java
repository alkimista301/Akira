package com.akira.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import com.akira.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    
    // Método para buscar por nombre (ignorando mayúsculas/minúsculas)
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
    
    /**
     * Buscar productos por categoría ID
     */
    @Query("SELECT p FROM Producto p WHERE p.categoria.id = :categoriaId")
    List<Producto> findByCategoriaId(@Param("categoriaId") Integer categoriaId);

    /**
     * Buscar productos por marca ID
     */
    @Query("SELECT p FROM Producto p WHERE p.marca.id = :marcaId")
    List<Producto> findByMarcaId(@Param("marcaId") Integer marcaId);

    /**
     * Buscar productos con stock mayor a cero
     */
    @Query("SELECT p FROM Producto p WHERE p.cantidadStock > 0")
    List<Producto> findByStockMayorQueCero();

    /**
     * Buscar productos por rango de precio
     */
    @Query("SELECT p FROM Producto p WHERE p.precio BETWEEN :precioMin AND :precioMax")
    List<Producto> findByPrecioBetween(@Param("precioMin") Double precioMin, @Param("precioMax") Double precioMax);

    /**
     * Buscar productos por modelo (contiene el texto)
     */
    List<Producto> findByModeloContainingIgnoreCase(String modelo);

    /**
     * Buscar productos con stock bajo
     */
    @Query("SELECT p FROM Producto p WHERE p.cantidadStock <= :cantidad")
    List<Producto> findByStockBajo(@Param("cantidad") Integer cantidad);

    /**
     * Buscar productos ordenados por precio ascendente
     */
    @Query("SELECT p FROM Producto p ORDER BY p.precio ASC")
    List<Producto> findAllOrderByPrecioAsc();

    /**
     * Buscar productos ordenados por precio descendente
     */
    @Query("SELECT p FROM Producto p ORDER BY p.precio DESC")
    List<Producto> findAllOrderByPrecioDesc();

    /**
     * Buscar productos ordenados por stock ascendente
     */
    @Query("SELECT p FROM Producto p ORDER BY p.cantidadStock ASC")
    List<Producto> findAllOrderByStockAsc();

    /**
     * Contar productos por categoría
     */
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.categoria.id = :categoriaId")
    long countByCategoriaId(@Param("categoriaId") Integer categoriaId);

    /**
     * Contar productos por marca
     */
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.marca.id = :marcaId")
    long countByMarcaId(@Param("marcaId") Integer marcaId);

    // ========== MÉTODOS ADICIONALES PARA ORDENAMIENTO ==========
    
    /**
     * Buscar por categoría ordenado por precio ascendente
     */
    @Query("SELECT p FROM Producto p WHERE p.categoria.id = :categoriaId ORDER BY p.precio ASC")
    List<Producto> findByCategoriaIdOrderByPrecioAsc(@Param("categoriaId") Integer categoriaId);

    /**
     * Buscar por categoría ordenado por precio descendente
     */
    @Query("SELECT p FROM Producto p WHERE p.categoria.id = :categoriaId ORDER BY p.precio DESC")
    List<Producto> findByCategoriaIdOrderByPrecioDesc(@Param("categoriaId") Integer categoriaId);

    /**
     * Buscar productos ordenados por nombre
     */
    @Query("SELECT p FROM Producto p ORDER BY p.nombre ASC")
    List<Producto> findAllOrderByNombreAsc();

    /**
     * Buscar productos más recientes (por ID)
     */
    @Query("SELECT p FROM Producto p ORDER BY p.idProducto DESC")
    List<Producto> findProductosMasRecientes();
}