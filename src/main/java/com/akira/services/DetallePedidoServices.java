package com.akira.services;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.akira.model.DetallePedido;
import com.akira.model.OrdenPedido;
import com.akira.model.Producto;
import com.akira.repository.DetallePedidoRepository;

@Service
public class DetallePedidoServices {

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    /**
     * Buscar detalles por ID de orden
     */
    public List<DetallePedido> buscarPorOrdenId(Integer ordenId) {
        try {
            return detallePedidoRepository.findByOrdenId(ordenId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al buscar detalles por orden: " + e.getMessage());
        }
    }

    /**
     * Buscar detalles por ID de producto
     */
    public List<DetallePedido> buscarPorProductoId(Integer productoId) {
        try {
            return detallePedidoRepository.findByProductoId(productoId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al buscar detalles por producto: " + e.getMessage());
        }
    }

    /**
     * Obtener productos de una orden con información detallada
     */
    public List<Map<String, Object>> obtenerProductosDeOrden(Integer ordenId) {
        try {
            List<DetallePedido> detalles = detallePedidoRepository.findByOrdenIdWithProductoAndOrden(ordenId);
            List<Map<String, Object>> productos = new ArrayList<>();
            
            for (DetallePedido detalle : detalles) {
                Map<String, Object> producto = new HashMap<>();
                producto.put("idDetalle", detalle.getIdDetalle());
                producto.put("cantidad", detalle.getCantidad());
                producto.put("subtotal", detalle.getSubtotal());
                
                if (detalle.getProducto() != null) {
                    Producto prod = detalle.getProducto();
                    producto.put("codigo", prod.getIdProducto());
                    producto.put("nombre", prod.getNombre());
                    producto.put("precioUnitario", prod.getPrecio());
                    producto.put("modelo", prod.getModelo() != null ? prod.getModelo() : "Sin modelo");
                    producto.put("marca", prod.getMarca() != null ? prod.getMarca().getNombre() : "Sin marca");
                    producto.put("categoria", prod.getCategoria() != null ? prod.getCategoria().getNombre() : "Sin categoría");
                } else {
                    // Producto no encontrado o eliminado
                    producto.put("codigo", 0);
                    producto.put("nombre", "Producto no disponible");
                    producto.put("precioUnitario", 0.0);
                    producto.put("modelo", "N/A");
                    producto.put("marca", "N/A");
                    producto.put("categoria", "N/A");
                }
                
                productos.add(producto);
            }
            
            return productos;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al obtener productos de la orden: " + e.getMessage());
        }
    }

    /**
     * Listar todos los detalles de pedido
     */
    public List<DetallePedido> listarTodos() {
        try {
            return detallePedidoRepository.findAll();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al listar detalles de pedido: " + e.getMessage());
        }
    }

    /**
     * Buscar detalle por ID
     */
    public DetallePedido buscarPorID(Integer id) {
        try {
            return detallePedidoRepository.findById(id).orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Registrar nuevo detalle de pedido
     */
    public DetallePedido registrar(DetallePedido detalle) {
        try {
            // Validaciones
            if (detalle.getOrden() == null) {
                throw new RuntimeException("La orden es obligatoria para el detalle");
            }
            
            if (detalle.getProducto() == null) {
                throw new RuntimeException("El producto es obligatorio para el detalle");
            }
            
            if (detalle.getCantidad() == null || detalle.getCantidad() <= 0) {
                throw new RuntimeException("La cantidad debe ser mayor a 0");
            }
            
            return detallePedidoRepository.save(detalle);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al registrar detalle de pedido: " + e.getMessage());
        }
    }

    /**
     * Actualizar detalle existente
     */
    public DetallePedido actualizar(DetallePedido detalle) {
        try {
            if (detalle.getIdDetalle() == null || !detallePedidoRepository.existsById(detalle.getIdDetalle())) {
                throw new RuntimeException("Detalle no encontrado para actualizar");
            }
            
            return detallePedidoRepository.save(detalle);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar detalle de pedido: " + e.getMessage());
        }
    }

    /**
     * Eliminar detalle
     */
    public void eliminar(Integer id) {
        try {
            if (!detallePedidoRepository.existsById(id)) {
                throw new RuntimeException("Detalle no encontrado para eliminar");
            }
            
            detallePedidoRepository.deleteById(id);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al eliminar detalle de pedido: " + e.getMessage());
        }
    }

    /**
     * Eliminar todos los detalles de una orden
     */
    @Transactional
    public void eliminarPorOrdenId(Integer ordenId) {
        try {
            detallePedidoRepository.deleteByOrdenId(ordenId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al eliminar detalles de la orden: " + e.getMessage());
        }
    }

    /**
     * Contar detalles por orden
     */
    public long contarPorOrden(Integer ordenId) {
        try {
            return detallePedidoRepository.countByOrdenId(ordenId);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Obtener total de cantidad de productos por orden
     */
    public Integer getTotalCantidadPorOrden(Integer ordenId) {
        try {
            Integer total = detallePedidoRepository.getTotalCantidadByOrdenId(ordenId);
            return total != null ? total : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Verificar si un producto está siendo usado en algún pedido
     */
    public boolean productoEnUso(Integer productoId) {
        try {
            return detallePedidoRepository.existsByProductoId(productoId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtener productos más vendidos
     */
    public List<Map<String, Object>> getProductosMasVendidos() {
        try {
            List<Object[]> resultados = detallePedidoRepository.getProductosMasVendidos();
            List<Map<String, Object>> productos = new ArrayList<>();
            
            for (Object[] resultado : resultados) {
                Map<String, Object> producto = new HashMap<>();
                producto.put("productoId", resultado[0]);
                producto.put("totalVendido", resultado[1]);
                productos.add(producto);
            }
            
            return productos;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al obtener productos más vendidos: " + e.getMessage());
        }
    }

    /**
     * Calcular total de una orden basado en sus detalles
     */
    public Double calcularTotalOrden(Integer ordenId) {
        try {
            List<DetallePedido> detalles = buscarPorOrdenId(ordenId);
            double total = 0.0;
            
            for (DetallePedido detalle : detalles) {
                total += detalle.getSubtotal();
            }
            
            return total;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }
 // REEMPLAZA tu método generarHTMLPedido en DetallePedidoServices.java con esta versión:

 // REEMPLAZA tu método generarHTMLPedido con esta versión MODERNA:

    /**
     * Generar HTML moderno para descarga de pedido
     */
    public byte[] generarHTMLPedido(OrdenPedido pedido, List<Map<String, Object>> productos) {
        try {
            StringBuilder html = new StringBuilder();
            
            html.append("<!DOCTYPE html>");
            html.append("<html>");
            html.append("<head>");
            html.append("<meta charset='UTF-8'>");
            html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            html.append("<title>Pedido ").append(pedido.getId()).append(" - Akira Computer</title>");
            html.append("<style>");
            
            // CSS MODERNO Y ATRACTIVO
            html.append("* { margin: 0; padding: 0; box-sizing: border-box; }");
            html.append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; ");
            html.append("background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); ");
            html.append("min-height: 100vh; padding: 20px; }");
            
            html.append(".container { max-width: 800px; margin: 0 auto; ");
            html.append("background: rgba(255,255,255,0.95); border-radius: 20px; ");
            html.append("box-shadow: 0 20px 40px rgba(0,0,0,0.1); overflow: hidden; }");
            
            html.append(".header { background: linear-gradient(135deg, #2c3e50, #3498db); ");
            html.append("color: white; padding: 30px; text-align: center; }");
            html.append(".header h1 { font-size: 2.5rem; margin-bottom: 10px; font-weight: 300; }");
            html.append(".header .subtitle { font-size: 1.2rem; opacity: 0.9; }");
            
            html.append(".content { padding: 30px; }");
            
            html.append(".info-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); ");
            html.append("gap: 20px; margin-bottom: 30px; }");
            
            html.append(".info-card { background: linear-gradient(135deg, #f8f9fa, #e9ecef); ");
            html.append("padding: 20px; border-radius: 15px; border-left: 5px solid #3498db; ");
            html.append("box-shadow: 0 5px 15px rgba(0,0,0,0.08); }");
            
            html.append(".info-card h3 { color: #2c3e50; margin-bottom: 15px; ");
            html.append("font-size: 1.3rem; display: flex; align-items: center; }");
            
            html.append(".info-card .icon { margin-right: 10px; font-size: 1.5rem; }");
            
            html.append(".info-item { margin-bottom: 10px; }");
            html.append(".info-item strong { color: #34495e; }");
            html.append(".info-item span { color: #7f8c8d; margin-left: 5px; }");
            
            html.append(".total-section { background: linear-gradient(135deg, #27ae60, #2ecc71); ");
            html.append("color: white; padding: 25px; border-radius: 15px; text-align: center; ");
            html.append("margin: 30px 0; box-shadow: 0 10px 25px rgba(39,174,96,0.3); }");
            
            html.append(".total-section h2 { font-size: 2rem; margin-bottom: 10px; }");
            html.append(".total-section .amount { font-size: 3rem; font-weight: bold; }");
            
            html.append(".description-section { background: linear-gradient(135deg, #f39c12, #e67e22); ");
            html.append("color: white; padding: 25px; border-radius: 15px; margin: 20px 0; }");
            
            html.append(".description-section h3 { margin-bottom: 15px; font-size: 1.5rem; }");
            html.append(".description-content { background: rgba(255,255,255,0.1); ");
            html.append("padding: 15px; border-radius: 10px; font-family: monospace; ");
            html.append("white-space: pre-wrap; line-height: 1.5; }");
            
            html.append(".observations-section { background: linear-gradient(135deg, #9b59b6, #8e44ad); ");
            html.append("color: white; padding: 25px; border-radius: 15px; margin: 20px 0; }");
            
            html.append(".footer { text-align: center; padding: 20px; ");
            html.append("background: #34495e; color: white; margin-top: 30px; }");
            
            html.append(".badge { display: inline-block; padding: 5px 15px; ");
            html.append("border-radius: 20px; font-size: 0.9rem; font-weight: bold; }");
            html.append(".badge-success { background: #27ae60; color: white; }");
            html.append(".badge-warning { background: #f39c12; color: white; }");
            html.append(".badge-info { background: #3498db; color: white; }");
            
            html.append("@media print { body { background: white; } .container { box-shadow: none; } }");
            html.append("</style>");
            html.append("</head>");
            html.append("<body>");
            
            html.append("<div class='container'>");
            
            // HEADER MODERNO
            html.append("<div class='header'>");
            html.append("<h1>🖥️ AKIRA COMPUTER</h1>");
            html.append("<div class='subtitle'>Detalle de Pedido #").append(pedido.getId()).append("</div>");
            html.append("</div>");
            
            html.append("<div class='content'>");
            
            // INFORMACIÓN EN TARJETAS
            html.append("<div class='info-grid'>");
            
            // Tarjeta 1: Información del Pedido
            html.append("<div class='info-card'>");
            html.append("<h3><span class='icon'>📋</span> Información del Pedido</h3>");
            html.append("<div class='info-item'><strong>ID:</strong><span>").append(pedido.getId()).append("</span></div>");
            
            if (pedido.getFecha() != null) {
                html.append("<div class='info-item'><strong>Fecha:</strong><span>").append(pedido.getFecha().toString()).append("</span></div>");
            }
            
            if (pedido.getTipoPedido() != null) {
                html.append("<div class='info-item'><strong>Tipo:</strong><span>").append(pedido.getTipoPedido()).append("</span></div>");
            }
            
            if (pedido.getEstado() != null) {
                String estadoClass = "badge-info";
                if ("COMPLETADO".equals(pedido.getEstado().getDescripcion())) estadoClass = "badge-success";
                else if ("PENDIENTE".equals(pedido.getEstado().getDescripcion())) estadoClass = "badge-warning";
                
                html.append("<div class='info-item'><strong>Estado:</strong> ");
                html.append("<span class='badge ").append(estadoClass).append("'>").append(pedido.getEstado().getDescripcion()).append("</span></div>");
            }
            html.append("</div>");
            
            // Tarjeta 2: Información del Cliente
            if (pedido.getCliente() != null) {
                html.append("<div class='info-card'>");
                html.append("<h3><span class='icon'>👤</span> Cliente</h3>");
                html.append("<div class='info-item'><strong>Nombre:</strong><span>").append(pedido.getCliente().getNombre()).append(" ").append(pedido.getCliente().getApellido()).append("</span></div>");
                html.append("<div class='info-item'><strong>DNI:</strong><span>").append(pedido.getCliente().getDni()).append("</span></div>");
                html.append("</div>");
            }
            
            html.append("</div>");
            
            // TOTAL DESTACADO
            html.append("<div class='total-section'>");
            html.append("<h2>💰 Total del Pedido</h2>");
            html.append("<div class='amount'>S/ ").append(pedido.getTotal() != null ? String.format("%.2f", pedido.getTotal()) : "0.00").append("</div>");
            html.append("</div>");
            
            // DESCRIPCIÓN DE COMPONENTES (solo si existe)
            if (pedido.getDetallesComponentes() != null && !pedido.getDetallesComponentes().isEmpty()) {
                html.append("<div class='description-section'>");
                html.append("<h3>🛠️ Descripción del Pedido</h3>");
                html.append("<div class='description-content'>").append(pedido.getDetallesComponentes()).append("</div>");
                html.append("</div>");
            }
            
            // OBSERVACIONES (solo si existe)
            if (pedido.getObservaciones() != null && !pedido.getObservaciones().isEmpty()) {
                html.append("<div class='observations-section'>");
                html.append("<h3>📝 Observaciones</h3>");
                html.append("<p>").append(pedido.getObservaciones()).append("</p>");
                html.append("</div>");
            }
            
            html.append("</div>");
            
            // FOOTER
            html.append("<div class='footer'>");
            html.append("Documento generado: ").append(java.time.LocalDateTime.now().toString());
            html.append("<br>");
            html.append("Akira Computer - Sistema de Gestión de Pedidos");
            html.append("</div>");
            
            html.append("</div>");
            html.append("</body>");
            html.append("</html>");
            
            return html.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al generar HTML: " + e.getMessage());
        }
    }
 
//AGREGAR ESTE MÉTODO DEBUG en DetallePedidoServices.java

/**
* Método debug para verificar productos de orden
*/
public List<Map<String, Object>> obtenerProductosDeOrdenDebug(Integer ordenId) {
  try {
      System.out.println("=== DEBUG OBTENER PRODUCTOS ===");
      System.out.println("Buscando productos para orden ID: " + ordenId);
      
      // Primero verificar si existen detalles para esta orden
      List<DetallePedido> detalles = detallePedidoRepository.findByOrdenId(ordenId);
      System.out.println("Detalles encontrados: " + detalles.size());
      
      if (detalles.isEmpty()) {
          System.out.println("❌ NO SE ENCONTRARON DETALLES PARA LA ORDEN " + ordenId);
          System.out.println("Verificar que existan registros en detalle_pedido para id_orden = " + ordenId);
      }
      
      for (DetallePedido detalle : detalles) {
          System.out.println("- Detalle ID: " + detalle.getIdDetalle());
          System.out.println("- Producto ID: " + (detalle.getProducto() != null ? detalle.getProducto().getIdProducto() : "NULL"));
          System.out.println("- Cantidad: " + detalle.getCantidad());
      }
      
      // Usar el método original
      return obtenerProductosDeOrden(ordenId);
      
  } catch (Exception e) {
      e.printStackTrace();
      System.out.println("❌ ERROR en obtenerProductosDeOrdenDebug: " + e.getMessage());
      return new ArrayList<>();
  }
}
 
}