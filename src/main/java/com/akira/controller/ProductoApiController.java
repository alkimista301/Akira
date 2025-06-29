package com.akira.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.akira.model.Producto;
import com.akira.model.Categoria;
import com.akira.services.ProductoServices;
import com.akira.services.CategoriaServices;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*")
public class ProductoApiController {

    @Autowired
    private ProductoServices productoServices;
    
    @Autowired
    private CategoriaServices categoriaServices;

    /**
     * Obtener productos por categoría (para el acordeón de armar-pc)
     */
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<Producto>> obtenerProductosPorCategoria(@PathVariable Integer categoriaId) {
        try {
            List<Producto> productos = productoServices.buscarPorCategoria(categoriaId);
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            System.err.println("Error al obtener productos por categoría: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Buscar productos por nombre (para búsquedas)
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<Producto>> buscarProductos(@RequestParam String nombre) {
        try {
            List<Producto> productos = productoServices.buscarPorNombre(nombre);
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            System.err.println("Error al buscar productos: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtener producto por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerProductoPorId(@PathVariable Integer id) {
        try {
            Producto producto = productoServices.buscarPorID(id);
            if (producto != null) {
                return ResponseEntity.ok(producto);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Error al obtener producto por ID: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtener producto por código
     */
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<Producto> obtenerProductoPorCodigo(@PathVariable String codigo) {
        try {
            Producto producto = productoServices.buscarPorCodigo(codigo);
            if (producto != null) {
                return ResponseEntity.ok(producto);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Error al obtener producto por código: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtener todos los productos activos
     */
    @GetMapping("/activos")
    public ResponseEntity<List<Producto>> obtenerProductosActivos() {
        try {
            List<Producto> productos = productoServices.listarActivos();
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            System.err.println("Error al obtener productos activos: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtener productos con stock disponible
     */
    @GetMapping("/con-stock")
    public ResponseEntity<List<Producto>> obtenerProductosConStock() {
        try {
            List<Producto> productos = productoServices.listarConStock();
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            System.err.println("Error al obtener productos con stock: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Verificar disponibilidad de productos para armar PC
     */
    @GetMapping("/verificar-disponibilidad")
    public ResponseEntity<Map<String, Object>> verificarDisponibilidad(@RequestParam List<Integer> productosIds) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean todosDisponibles = true;
            Map<Integer, Boolean> disponibilidad = new HashMap<>();
            
            for (Integer productoId : productosIds) {
                Producto producto = productoServices.buscarPorID(productoId);
                // CORREGIDO: usar getActivo() y getCantidadStock()
                boolean disponible = producto != null &&  producto.getCantidadStock() > 0;
                disponibilidad.put(productoId, disponible);
                
                if (!disponible) {
                    todosDisponibles = false;
                }
            }
            
            response.put("success", true);
            response.put("todosDisponibles", todosDisponibles);
            response.put("disponibilidad", disponibilidad);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al verificar disponibilidad: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Obtener información de categorías para el frontend
     */
    @GetMapping("/categorias")
    public ResponseEntity<List<Categoria>> obtenerCategorias() {
        try {
            List<Categoria> categorias = categoriaServices.listarTodas();
            return ResponseEntity.ok(categorias);
        } catch (Exception e) {
            System.err.println("Error al obtener categorías: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtener productos recomendados para armar PC
     */
    @GetMapping("/recomendados")
    public ResponseEntity<Map<String, List<Producto>>> obtenerProductosRecomendados() {
        Map<String, List<Producto>> recomendaciones = new HashMap<>();
        try {
            // Obtener categorías principales para armar PC
            List<Categoria> categorias = categoriaServices.listarTodas();
            
            for (Categoria categoria : categorias) {
                List<Producto> productos = productoServices.buscarPorCategoria(categoria.getId());
                // Filtrar solo productos activos y con stock
                productos = productos.stream()
                    .filter(p -> p.getCantidadStock() > 0)
                    .limit(5) // Limitar a 5 productos recomendados por categoría
                    .toList();
                    
                recomendaciones.put(categoria.getNombre(), productos);
            }
            
            return ResponseEntity.ok(recomendaciones);
        } catch (Exception e) {
            System.err.println("Error al obtener productos recomendados: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Calcular precio total de configuración de PC
     */
    @GetMapping("/calcular-total")
    public ResponseEntity<Map<String, Object>> calcularTotalConfiguracion(@RequestParam List<Integer> productosIds) {
        Map<String, Object> response = new HashMap<>();
        try {
            double total = 0;
            Map<Integer, Map<String, Object>> detalles = new HashMap<>();
            
            for (Integer productoId : productosIds) {
                Producto producto = productoServices.buscarPorID(productoId);
                if (producto != null) {
                    total += producto.getPrecio();
                    
                    Map<String, Object> detalle = new HashMap<>();
                    detalle.put("nombre", producto.getNombre());
                    detalle.put("precio", producto.getPrecio());
                    detalle.put("disponible", producto.getCantidadStock() > 0);
                    detalles.put(productoId, detalle);
                }
            }
            
            response.put("success", true);
            response.put("total", total);
            response.put("detalles", detalles);
            response.put("cantidadProductos", productosIds.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al calcular total: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}