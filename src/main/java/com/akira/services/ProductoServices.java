package com.akira.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.akira.model.Producto;
import com.akira.repository.ProductoRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductoServices {

    @Autowired
    private ProductoRepository productoRepository;

    // Listar todos los productos
    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    // Buscar producto por ID
    public Producto buscarPorID(Integer id) {
        return productoRepository.findById(id).orElse(null);
    }

    // Buscar productos por nombre
    public List<Producto> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    // Registrar nuevo producto
    public Producto registrar(Producto producto) {
        return productoRepository.save(producto);
    }

    // Actualizar producto existente
    public Producto actualizar(Producto producto) {
        if (producto.getIdProducto() != null && productoRepository.existsById(producto.getIdProducto())) {
            return productoRepository.save(producto);
        }
        throw new RuntimeException("Producto no encontrado para actualizar");
    }

    // Eliminar producto
    public void eliminar(Integer id) {
        if (productoRepository.existsById(id)) {
            productoRepository.deleteById(id);
        } else {
            throw new RuntimeException("Producto no encontrado para eliminar");
        }
    }

    // Buscar productos con stock bajo
    public List<Producto> buscarStockBajo(Integer cantidad) {
        return productoRepository.findByStockBajo(cantidad);
    }

    /**
     * Buscar productos por categoría
     */
    public List<Producto> buscarPorCategoria(Integer categoriaId) {
        try {
            return productoRepository.findByCategoriaId(categoriaId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al buscar productos por categoría: " + e.getMessage());
        }
    }

    /**
     * Buscar productos por marca
     */
    public List<Producto> buscarPorMarca(Integer marcaId) {
        try {
            return productoRepository.findByMarcaId(marcaId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al buscar productos por marca: " + e.getMessage());
        }
    }

    /**
     * Buscar productos con stock disponible
     */
    public List<Producto> buscarConStock() {
        try {
            return productoRepository.findByStockMayorQueCero();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al buscar productos con stock: " + e.getMessage());
        }
    }

    /**
     * Buscar productos por rango de precio
     */
    public List<Producto> buscarPorRangoPrecio(Double precioMin, Double precioMax) {
        try {
            return productoRepository.findByPrecioBetween(precioMin, precioMax);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al buscar productos por rango de precio: " + e.getMessage());
        }
    }

    /**
     * Buscar productos por modelo
     */
    public List<Producto> buscarPorModelo(String modelo) {
        try {
            return productoRepository.findByModeloContainingIgnoreCase(modelo);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al buscar productos por modelo: " + e.getMessage());
        }
    }

    /**
     * Verificar disponibilidad de stock
     */
    public boolean verificarStock(Integer productoId, Integer cantidadRequerida) {
        try {
            Producto producto = buscarPorID(productoId);
            return producto != null && producto.getCantidadStock() >= cantidadRequerida;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Reducir stock de producto
     */
    public boolean reducirStock(Integer productoId, Integer cantidad) {
        try {
            Producto producto = buscarPorID(productoId);
            if (producto != null && producto.getCantidadStock() >= cantidad) {
                producto.setCantidadStock(producto.getCantidadStock() - cantidad);
                actualizar(producto);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Aumentar stock de producto
     */
    public boolean aumentarStock(Integer productoId, Integer cantidad) {
        try {
            Producto producto = buscarPorID(productoId);
            if (producto != null) {
                producto.setCantidadStock(producto.getCantidadStock() + cantidad);
                actualizar(producto);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ========== MÉTODOS QUE REQUERÍA EL CONTROLLER ==========
    
    /**
     * Listar productos "activos" (todos los productos disponibles)
     * Equivalente a listar todos los productos
     */
    public List<Producto> listarActivos() {
        try {
            return productoRepository.findAll();
        } catch (Exception e) {
            System.err.println("Error al listar productos activos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Listar productos con stock disponible
     * Alias para buscarConStock()
     */
    public List<Producto> listarConStock() {
        return buscarConStock();
    }

    /**
     * Buscar producto por código (usando ID)
     */
    public Producto buscarPorCodigo(String codigo) {
        try {
            Integer id = Integer.parseInt(codigo);
            return buscarPorID(id);
        } catch (NumberFormatException e) {
            System.err.println("Código de producto inválido: " + codigo);
            return null;
        } catch (Exception e) {
            System.err.println("Error al buscar producto por código: " + e.getMessage());
            return null;
        }
    }

    /**
     * Verificar si un producto tiene stock suficiente
     */
    public boolean tieneStock(Integer productoId, Integer cantidadRequerida) {
        return verificarStock(productoId, cantidadRequerida);
    }

    /**
     * Obtener productos por categoría ordenados por precio
     */
    public List<Producto> buscarPorCategoriaOrdenadoPorPrecio(Integer categoriaId, boolean ascendente) {
        try {
            if (ascendente) {
                return productoRepository.findByCategoriaIdOrderByPrecioAsc(categoriaId);
            } else {
                return productoRepository.findByCategoriaIdOrderByPrecioDesc(categoriaId);
            }
        } catch (Exception e) {
            System.err.println("Error al buscar productos por categoría ordenados: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Generar PDF con lista de productos
     */
    public byte[] generarPDFProductos(List<Producto> productos) {
        try {
            StringBuilder html = new StringBuilder();
            
            // Encabezado del PDF
            html.append("<!DOCTYPE html>");
            html.append("<html><head>");
            html.append("<meta charset='UTF-8'>");
            html.append("<style>");
            html.append("body { font-family: Arial, sans-serif; margin: 20px; }");
            html.append("h1 { color: #333; text-align: center; }");
            html.append("table { width: 100%; border-collapse: collapse; margin-top: 20px; }");
            html.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
            html.append("th { background-color: #f2f2f2; font-weight: bold; }");
            html.append("tr:nth-child(even) { background-color: #f9f9f9; }");
            html.append(".header-info { text-align: center; margin-bottom: 20px; }");
            html.append(".precio { text-align: right; }");
            html.append(".stock-bajo { background-color: #ffebee; }");
            html.append("</style>");
            html.append("</head><body>");
            
            // Contenido del PDF
            html.append("<div class='header-info'>");
            html.append("<h1>AKIRA COMPUTER</h1>");
            html.append("<h2>Lista de Productos</h2>");
            html.append("<p>Fecha de generación: ").append(new java.util.Date()).append("</p>");
            html.append("<p>Total de productos: ").append(productos.size()).append("</p>");
            html.append("</div>");
            
            // Tabla de productos
            html.append("<table>");
            html.append("<thead>");
            html.append("<tr>");
            html.append("<th>Código</th>");
            html.append("<th>Nombre</th>");
            html.append("<th>Categoría</th>");
            html.append("<th>Marca</th>");
            html.append("<th>Modelo</th>");
            html.append("<th>Precio (S/)</th>");
            html.append("<th>Stock</th>");
            html.append("</tr>");
            html.append("</thead>");
            html.append("<tbody>");
            
            for (Producto producto : productos) {
                String filaClass = producto.getCantidadStock() <= 5 ? " class='stock-bajo'" : "";
                html.append("<tr").append(filaClass).append(">");
                html.append("<td>").append(producto.getIdProducto()).append("</td>");
                html.append("<td>").append(producto.getNombre()).append("</td>");
                html.append("<td>").append(producto.getCategoria() != null ? producto.getCategoria().getNombre() : "Sin categoría").append("</td>");
                html.append("<td>").append(producto.getMarca() != null ? producto.getMarca().getNombre() : "Sin marca").append("</td>");
                html.append("<td>").append(producto.getModelo() != null ? producto.getModelo() : "-").append("</td>");
                html.append("<td class='precio'>").append(String.format("%.2f", producto.getPrecio())).append("</td>");
                html.append("<td>").append(producto.getCantidadStock()).append("</td>");
                html.append("</tr>");
            }
            
            html.append("</tbody>");
            html.append("</table>");
            html.append("</body></html>");
            
            // Convertir HTML a PDF usando una librería simple
            return generarPDFDesdeHTML(html.toString());
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al generar PDF: " + e.getMessage());
        }
    }

    
    /**
     * Método auxiliar para convertir HTML a PDF (versión simple)
     */
    private byte[] generarPDFDesdeHTML(String html) {
        // Generar un archivo HTML que se puede guardar como PDF
        StringBuilder contenido = new StringBuilder();
        contenido.append(html);
        contenido.append("<script>");
        contenido.append("window.onload = function() { window.print(); };");
        contenido.append("</script>");
        
        return contenido.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    
    }
}