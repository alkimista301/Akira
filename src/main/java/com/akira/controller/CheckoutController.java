package com.akira.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.akira.model.Cliente;
import com.akira.model.OrdenPedido;
import com.akira.model.Producto;
import com.akira.services.ClienteServices;
import com.akira.services.OrdenPedidoServices;
import com.akira.services.ProductoServices;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    @Autowired
    private ClienteServices clienteServices;
    
    @Autowired
    private OrdenPedidoServices ordenPedidoServices;
    
    @Autowired
    private ProductoServices productoServices;

    /**
     * Página de checkout/confirmación de pedido
     */
    @GetMapping
    public String checkout(Model model, HttpSession session) {
        try {
            // Verificar que el cliente esté logueado
            String clienteId = (String) session.getAttribute("clienteId");
            if (clienteId == null) {
                return "redirect:/?login=true";
            }
            
            Cliente cliente = clienteServices.buscarPorDni(clienteId);
            if (cliente == null) {
                model.addAttribute("error", "Cliente no encontrado");
                return "error";
            }
            
            model.addAttribute("cliente", cliente);
            return "checkout"; // archivo checkout.html
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar página de checkout: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Crear pedido de PC armada desde el cliente - USAR MÉTODO EXISTENTE
     */
    @PostMapping("/crear-pedido-pc")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> crearPedidoPC(
            @RequestBody Map<String, Object> datosPedido, 
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Verificar sesión del cliente
            String clienteId = (String) session.getAttribute("clienteId");
            if (clienteId == null) {
                response.put("success", false);
                response.put("message", "Debe iniciar sesión para crear un pedido");
                return ResponseEntity.badRequest().body(response);
            }
            
            Cliente cliente = clienteServices.buscarPorDni(clienteId);
            if (cliente == null) {
                response.put("success", false);
                response.put("message", "Cliente no encontrado");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Extraer datos del pedido
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> productos = (List<Map<String, Object>>) datosPedido.get("productos");
            Double total = Double.valueOf(datosPedido.get("total").toString());
            String observaciones = (String) datosPedido.get("observaciones");
            
            // Validar productos
            if (productos == null || productos.isEmpty()) {
                response.put("success", false);
                response.put("message", "Debe seleccionar al menos un componente");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Verificar stock y validar productos
            StringBuilder validationErrors = new StringBuilder();
            StringBuilder descripcionComponentes = new StringBuilder();
            double totalCalculado = 0;
            
            for (Map<String, Object> prodData : productos) {
                Integer productoId = (Integer) prodData.get("productoId");
                Integer cantidad = prodData.get("cantidad") != null ? (Integer) prodData.get("cantidad") : 1;
                
                Producto producto = productoServices.buscarPorID(productoId);
                if (producto == null) {
                    validationErrors.append("Producto con ID ").append(productoId).append(" no encontrado. ");
                    continue;
                }
              
                
                // CORREGIDO: usar getCantidadStock() en lugar de getStock()
                if (producto.getCantidadStock() < cantidad) {
                    validationErrors.append("Stock insuficiente para ").append(producto.getNombre())
                                  .append(" (disponible: ").append(producto.getCantidadStock()).append("). ");
                    continue;
                }
                
                // Agregar a descripción
                if (descripcionComponentes.length() > 0) {
                    descripcionComponentes.append(", ");
                }
                descripcionComponentes.append(producto.getNombre());
                if (cantidad > 1) {
                    descripcionComponentes.append(" (").append(cantidad).append("x)");
                }
                
                totalCalculado += producto.getPrecio().doubleValue() * cantidad;
            }
            
            if (validationErrors.length() > 0) {
                response.put("success", false);
                response.put("message", "Errores de validación: " + validationErrors.toString());
                return ResponseEntity.badRequest().body(response);
            }
            
            // Verificar que el total coincida (con tolerancia para decimales)
            if (Math.abs(total - totalCalculado) > 0.01) {
                response.put("success", false);
                response.put("message", "El total del pedido no coincide con los precios actuales");
                return ResponseEntity.badRequest().body(response);
            }
            
            // USAR EL MÉTODO EXISTENTE crearPedidoVendedor pero adaptado para cliente
            // Como no tenemos vendedor, usar null o un vendedor por defecto
            OrdenPedido pedidoCreado = ordenPedidoServices.crearPedidoVendedor(
                clienteId,                                      // DNI del cliente
                null,                                          // Sin vendedor (es cliente web)
                BigDecimal.valueOf(totalCalculado),           // Total
                "ARMAR_PC",                                    // Tipo
                descripcionComponentes.toString()             // Descripción
            );
            
            response.put("success", true);
            response.put("message", "Pedido creado exitosamente");
            response.put("pedido", pedidoCreado);
            response.put("pedidoId", pedidoCreado.getId());
            response.put("total", totalCalculado);
            response.put("redirectUrl", "/cliente-dashboard/" + clienteId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error al crear pedido: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Validar carrito antes del checkout
     */
    @PostMapping("/validar-carrito")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validarCarrito(@RequestBody Map<String, Object> datosCarrito) {
        Map<String, Object> response = new HashMap<>();
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> productos = (List<Map<String, Object>>) datosCarrito.get("productos");
            
            boolean todosDisponibles = true;
            StringBuilder errores = new StringBuilder();
            double totalActualizado = 0;
            
            for (Map<String, Object> prodData : productos) {
                Integer productoId = (Integer) prodData.get("productoId");
                Integer cantidad = prodData.get("cantidad") != null ? (Integer) prodData.get("cantidad") : 1;
                
                Producto producto = productoServices.buscarPorID(productoId);
                if (producto == null) {
                    todosDisponibles = false;
                    errores.append("Producto con ID ").append(productoId).append(" no encontrado. ");
                // CORREGIDO: usar getActivo() en lugar de isActivo()
                } else if (producto.getCantidadStock() < cantidad) {
                    todosDisponibles = false;
                    errores.append(producto.getNombre()).append(" tiene stock insuficiente (disponible: ")
                          .append(producto.getCantidadStock()).append("). ");
                } else {
                    totalActualizado += producto.getPrecio().doubleValue() * cantidad;
                }
            }
            
            response.put("success", true);
            response.put("todosDisponibles", todosDisponibles);
            response.put("errores", errores.toString());
            response.put("totalActualizado", totalActualizado);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al validar carrito: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Crear pedido de productos individuales desde el checkout
     */
    @PostMapping("/crear-pedido-productos")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> crearPedidoProductos(
            @RequestBody Map<String, Object> datosPedido, 
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Verificar sesión del cliente
            String clienteId = (String) session.getAttribute("clienteId");
            if (clienteId == null) {
                response.put("success", false);
                response.put("message", "Debe iniciar sesión para crear un pedido");
                return ResponseEntity.badRequest().body(response);
            }

            Cliente cliente = clienteServices.buscarPorDni(clienteId);
            if (cliente == null) {
                response.put("success", false);
                response.put("message", "Cliente no encontrado");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Extraer datos del pedido
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> productos = (List<Map<String, Object>>) datosPedido.get("productos");
            Double total = Double.valueOf(datosPedido.get("total").toString());
            String observaciones = (String) datosPedido.get("observaciones");
            
            // Validar productos
            if (productos == null || productos.isEmpty()) {
                response.put("success", false);
                response.put("message", "Debe seleccionar al menos un producto");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Verificar stock y validar productos (igual que el vendedor)
            StringBuilder validationErrors = new StringBuilder();
            StringBuilder descripcionProductos = new StringBuilder();
            double totalCalculado = 0;
            
            for (Map<String, Object> prodData : productos) {
                Integer productoId = (Integer) prodData.get("productoId");
                Integer cantidad = prodData.get("cantidad") != null ? (Integer) prodData.get("cantidad") : 1;
                
                Producto producto = productoServices.buscarPorID(productoId);
                if (producto == null) {
                    validationErrors.append("Producto con ID ").append(productoId).append(" no encontrado. ");
                    continue;
                }
                
                // Verificar stock
                if (producto.getCantidadStock() < cantidad) {
                    validationErrors.append("Stock insuficiente para ").append(producto.getNombre())
                                  .append(" (disponible: ").append(producto.getCantidadStock()).append("). ");
                    continue;
                }
                
                // Construir descripción de productos
                if (descripcionProductos.length() > 0) {
                    descripcionProductos.append(", ");
                }
                descripcionProductos.append(producto.getNombre());
                if (cantidad > 1) {
                    descripcionProductos.append(" (").append(cantidad).append("x)");
                }
                
                totalCalculado += producto.getPrecio().doubleValue() * cantidad;
            }
            
            if (validationErrors.length() > 0) {
                response.put("success", false);
                response.put("message", "Errores de validación: " + validationErrors.toString());
                return ResponseEntity.badRequest().body(response);
            }
            
            // Verificar que el total coincida (con tolerancia para decimales)
            if (Math.abs(total - totalCalculado) > 0.01) {
                response.put("success", false);
                response.put("message", "El total del pedido no coincide con los precios actuales");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Agregar observaciones a la descripción si existen
            String descripcionFinal = descripcionProductos.toString();
            if (observaciones != null && !observaciones.trim().isEmpty()) {
                descripcionFinal += " | Observaciones: " + observaciones.trim();
            }
            
            // Crear pedido usando el método para clientes
            OrdenPedido pedidoCreado = ordenPedidoServices.crearPedidoCliente(
                clienteId,                              // DNI del cliente
                BigDecimal.valueOf(totalCalculado),    // Total
                "PRODUCTO_COMPLETO",                   // Tipo
                descripcionFinal                       // Descripción con productos y observaciones
            );
            
            response.put("success", true);
            response.put("message", "Pedido creado exitosamente");
            response.put("pedidoId", pedidoCreado.getId());
            response.put("total", totalCalculado);
            response.put("redirectUrl", "/cliente-dashboard/" + clienteId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error al crear pedido: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}